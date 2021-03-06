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

import java.util.HashMap;
import java.util.Map;

public class InMemoryStoreStreamProvider implements StoreFileStreamProvider
{
    private Map<String,StringBuffer> fileStreams = new HashMap<>();

    @Override
    public StoreFileStream acquire( String destination, int requiredAlignment )
    {
        fileStreams.putIfAbsent( destination, new StringBuffer() );
        return new InMemoryStoreStream( fileStreams.get( destination ) );
    }

    public Map<String,StringBuffer> fileStreams()
    {
        return fileStreams;
    }

    class InMemoryStoreStream implements StoreFileStream
    {
        private StringBuffer stringBuffer;

        InMemoryStoreStream( StringBuffer stringBuffer )
        {
            this.stringBuffer = stringBuffer;
        }

        @Override
        public void write( byte[] data )
        {
            for ( byte b : data )
            {
                stringBuffer.append( (char) b );
            }
        }

        @Override
        public void close()
        {
            // do nothing
        }
    }
}
