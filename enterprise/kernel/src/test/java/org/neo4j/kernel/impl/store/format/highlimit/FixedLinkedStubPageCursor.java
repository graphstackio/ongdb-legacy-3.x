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
package org.neo4j.kernel.impl.store.format.highlimit;

import org.neo4j.io.pagecache.PageCursor;
import org.neo4j.io.pagecache.StubPageCursor;

class FixedLinkedStubPageCursor extends StubPageCursor
{
    FixedLinkedStubPageCursor( int initialPageId, int size )
    {
        super( initialPageId, size );
    }

    @Override
    public PageCursor openLinkedCursor( long pageId )
    {
        // Since we always assume here that test data will be small enough for one page it's safe
        // to assume that all cursors will be be positioned into that one page.
        // And since stub cursors use byte buffers to store data we want to prevent data loss and keep already
        // created linked cursors
        if ( linkedCursor == null )
        {
            return super.openLinkedCursor( pageId );
        }
        return linkedCursor;
    }
}
