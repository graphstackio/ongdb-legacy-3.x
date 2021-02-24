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
package org.neo4j.causalclustering.core.consensus.log.segmented;

import java.io.IOException;

import org.neo4j.causalclustering.core.consensus.log.EntryRecord;
import org.neo4j.causalclustering.core.consensus.log.RaftLogCursor;
import org.neo4j.causalclustering.core.consensus.log.RaftLogEntry;
import org.neo4j.cursor.CursorValue;
import org.neo4j.cursor.IOCursor;

class SegmentedRaftLogCursor implements RaftLogCursor
{
    private final IOCursor<EntryRecord> inner;
    private CursorValue<RaftLogEntry> current;
    private long index;

    SegmentedRaftLogCursor( long fromIndex, IOCursor<EntryRecord> inner )
    {
        this.inner = inner;
        this.current = new CursorValue<>();
        this.index = fromIndex - 1;
    }

    @Override
    public boolean next() throws IOException
    {
        boolean hasNext = inner.next();
        if ( hasNext )
        {
            current.set( inner.get().logEntry() );
            index++;
        }
        else
        {
            current.invalidate();
        }
        return hasNext;
    }

    @Override
    public void close() throws IOException
    {
        inner.close();
    }

    @Override
    public long index()
    {
        return index;
    }

    @Override
    public RaftLogEntry get()
    {
        return current.get();
    }
}
