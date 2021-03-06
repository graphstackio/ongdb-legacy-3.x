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

import org.neo4j.causalclustering.catchup.CatchUpResponseHandler;
import org.neo4j.causalclustering.catchup.CatchupClientProtocol;

import static org.neo4j.causalclustering.catchup.CatchupClientProtocol.State;

public class StoreCopyFinishedResponseHandler extends SimpleChannelInboundHandler<StoreCopyFinishedResponse>
{
    private final CatchupClientProtocol protocol;
    private CatchUpResponseHandler handler;

    public StoreCopyFinishedResponseHandler( CatchupClientProtocol protocol,
                                             CatchUpResponseHandler handler )
    {
        this.protocol = protocol;
        this.handler = handler;
    }

    @Override
    protected void channelRead0( ChannelHandlerContext ctx, final StoreCopyFinishedResponse msg )
    {
        handler.onFileStreamingComplete( msg );
        protocol.expect( State.MESSAGE_TYPE );
    }
}
