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
package org.neo4j.com;

import org.jboss.netty.buffer.ChannelBuffer;

import java.nio.ByteBuffer;

import org.neo4j.storageengine.api.StoreId;

public class Protocol214 extends Protocol
{
    public Protocol214( int chunkSize, byte applicationProtocolVersion, byte internalProtocolVersion )
    {
        super( chunkSize, applicationProtocolVersion, internalProtocolVersion );
    }

    @Override
    protected StoreId readStoreId( ChannelBuffer source, ByteBuffer byteBuffer )
    {
        byteBuffer.clear();
        byteBuffer.limit( 8 + 8 + 8 + 8 + 8 ); // creation time, random id, store version, upgrade time, upgrade id
        source.readBytes( byteBuffer );
        byteBuffer.flip();
        // read order matters - see Server.writeStoreId() for version 2.1.4
        long creationTime = byteBuffer.getLong();
        long randomId = byteBuffer.getLong();
        long storeVersion = byteBuffer.getLong();
        long upgradeTime = byteBuffer.getLong();
        long upgradeId = byteBuffer.getLong();
        return new StoreId( creationTime, randomId, storeVersion, upgradeTime, upgradeId );
    }
}
