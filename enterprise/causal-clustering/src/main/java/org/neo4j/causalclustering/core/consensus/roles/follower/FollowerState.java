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
package org.neo4j.causalclustering.core.consensus.roles.follower;

import static java.lang.String.format;

/**
 * Things the leader thinks it knows about a follower.
 */
public class FollowerState
{
    // We know that the follower agrees with our (leader) log up until this index. Only updated by the leader when:
    // * increased when it receives a successful AppendEntries.Response
    private final long matchIndex;

    public FollowerState()
    {
        this( -1 );
    }

    private FollowerState( long matchIndex )
    {
        assert matchIndex >= -1 : format( "Match index can never be less than -1. Was %d", matchIndex );
        this.matchIndex = matchIndex;
    }

    public long getMatchIndex()
    {
        return matchIndex;
    }

    public FollowerState onSuccessResponse( long newMatchIndex )
    {
        return new FollowerState( newMatchIndex );
    }

    @Override
    public String toString()
    {
        return format( "State{matchIndex=%d}", matchIndex );
    }
}
