/*
 * Copyright (c) 2018-2020 "Graph Foundation"
 * Graph Foundation, Inc. [https://graphfoundation.org]
 *
 * Copyright (c) 2002-2018 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of ONgDB Enterprise Edition. The included source
 * code can be redistributed and/or modified under the terms of the
 * GNU AFFERO GENERAL PUBLIC LICENSE Version 3
 * (http://www.fsf.org/licensing/licenses/agpl-3.0.html) with the
 * Commons Clause, as found
 * in the associated LICENSE.txt file.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 */
package org.neo4j.causalclustering.catchup.storecopy;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.neo4j.causalclustering.catchup.CatchupServerProtocol;
import org.neo4j.causalclustering.catchup.CheckPointerService;
import org.neo4j.causalclustering.messaging.StoreCopyRequest;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.helpers.collection.Iterators;
import org.neo4j.io.fs.FileSystemAbstraction;
import org.neo4j.kernel.NeoStoreDataSource;
import org.neo4j.logging.Log;
import org.neo4j.logging.LogProvider;
import org.neo4j.storageengine.api.StoreFileMetadata;

import static java.lang.String.format;
import static org.neo4j.causalclustering.catchup.storecopy.DataSourceChecks.hasSameStoreId;
import static org.neo4j.causalclustering.catchup.storecopy.DataSourceChecks.isTransactionWithinReach;
import static org.neo4j.io.fs.FileUtils.relativePath;

public abstract class StoreCopyRequestHandler<T extends StoreCopyRequest> extends SimpleChannelInboundHandler<T>
{
    private final CatchupServerProtocol protocol;
    private final Supplier<NeoStoreDataSource> dataSource;
    private final CheckPointerService checkPointerService;
    private final StoreFileStreamingProtocol storeFileStreamingProtocol;

    private final FileSystemAbstraction fs;
    private final Log log;

    StoreCopyRequestHandler( CatchupServerProtocol protocol, Supplier<NeoStoreDataSource> dataSource, CheckPointerService checkPointerService,
            StoreFileStreamingProtocol storeFileStreamingProtocol, FileSystemAbstraction fs, LogProvider logProvider )
    {
        this.protocol = protocol;
        this.dataSource = dataSource;
        this.storeFileStreamingProtocol = storeFileStreamingProtocol;
        this.fs = fs;
        this.log = logProvider.getLog( StoreCopyRequestHandler.class );
        this.checkPointerService = checkPointerService;
    }

    @Override
    protected void channelRead0( ChannelHandlerContext ctx, T request ) throws Exception
    {
        log.debug( "Handling request %s", request );
        StoreCopyFinishedResponse.Status responseStatus = StoreCopyFinishedResponse.Status.E_UNKNOWN;
        try
        {
            NeoStoreDataSource neoStoreDataSource = dataSource.get();
            if ( !hasSameStoreId( request.expectedStoreId(), neoStoreDataSource ) )
            {
                responseStatus = StoreCopyFinishedResponse.Status.E_STORE_ID_MISMATCH;
            }
            else if ( !isTransactionWithinReach( request.requiredTransactionId(), checkPointerService ) )
            {
                responseStatus = StoreCopyFinishedResponse.Status.E_TOO_FAR_BEHIND;
                checkPointerService.tryAsyncCheckpoint(
                        e -> log.error( "Failed to do a checkpoint that was invoked after a too far behind error on store copy request", e ) );
            }
            else
            {
                File databaseDirectory = neoStoreDataSource.getDatabaseLayout().databaseDirectory();
                try ( ResourceIterator<StoreFileMetadata> resourceIterator = files( request, neoStoreDataSource ) )
                {
                    while ( resourceIterator.hasNext() )
                    {
                        StoreFileMetadata storeFileMetadata = resourceIterator.next();
                        StoreResource storeResource = new StoreResource( storeFileMetadata.file(), relativePath( databaseDirectory, storeFileMetadata.file() ),
                                storeFileMetadata.recordSize(), fs );
                        storeFileStreamingProtocol.stream( ctx, storeResource );
                    }
                }
                responseStatus = StoreCopyFinishedResponse.Status.SUCCESS;
            }
        }
        finally
        {
            storeFileStreamingProtocol.end( ctx, responseStatus );
            protocol.expect( CatchupServerProtocol.State.MESSAGE_TYPE );
        }
    }

    abstract ResourceIterator<StoreFileMetadata> files( T request, NeoStoreDataSource neoStoreDataSource ) throws IOException;

    private static Iterator<StoreFileMetadata> onlyOne( List<StoreFileMetadata> files, String description )
    {
        if ( files.size() != 1 )
        {
            throw new IllegalStateException( format( "Expected exactly one file '%s'. Got %d", description, files.size() ) );
        }
        return files.iterator();
    }

    private static Predicate<StoreFileMetadata> matchesRequested( String fileName )
    {
        return f -> f.file().getName().equals( fileName );
    }

    public static class GetStoreFileRequestHandler extends StoreCopyRequestHandler<GetStoreFileRequest>
    {
        public GetStoreFileRequestHandler( CatchupServerProtocol protocol, Supplier<NeoStoreDataSource> dataSource, CheckPointerService checkPointerService,
                StoreFileStreamingProtocol storeFileStreamingProtocol, FileSystemAbstraction fs, LogProvider logProvider )
        {
            super( protocol, dataSource, checkPointerService, storeFileStreamingProtocol, fs, logProvider );
        }

        @Override
        ResourceIterator<StoreFileMetadata> files( GetStoreFileRequest request, NeoStoreDataSource neoStoreDataSource ) throws IOException
        {
            try ( ResourceIterator<StoreFileMetadata> resourceIterator = neoStoreDataSource.listStoreFiles( false ) )
            {
                String fileName = request.file().getName();
                return Iterators.asResourceIterator(
                        onlyOne( resourceIterator.stream().filter( matchesRequested( fileName ) ).collect( Collectors.toList() ), fileName ) );
            }
        }
    }

    public static class GetIndexSnapshotRequestHandler extends StoreCopyRequestHandler<GetIndexFilesRequest>
    {
        public GetIndexSnapshotRequestHandler( CatchupServerProtocol protocol, Supplier<NeoStoreDataSource> dataSource,
                CheckPointerService checkPointerService, StoreFileStreamingProtocol storeFileStreamingProtocol,
                FileSystemAbstraction fs, LogProvider logProvider )
        {
            super( protocol, dataSource, checkPointerService, storeFileStreamingProtocol, fs, logProvider );
        }

        @Override
        ResourceIterator<StoreFileMetadata> files( GetIndexFilesRequest request, NeoStoreDataSource neoStoreDataSource ) throws IOException
        {
            return neoStoreDataSource.getNeoStoreFileListing().getNeoStoreFileIndexListing().getSnapshot( request.indexId() );
        }
    }
}
