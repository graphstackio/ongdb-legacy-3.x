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
package org.neo4j.causalclustering.core.consensus.membership;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.neo4j.causalclustering.identity.MemberId;
import org.neo4j.causalclustering.messaging.marshalling.ReplicatedContentHandler;

import static java.lang.String.format;
import static org.neo4j.causalclustering.identity.RaftTestMember.member;

public class RaftTestGroup implements RaftGroup<MemberId>
{
    private final Set<MemberId> members = new HashSet<>();

    public RaftTestGroup( Set<MemberId> members )
    {
        this.members.addAll( members );
    }

    public RaftTestGroup( int... memberIds )
    {
        for ( int memberId : memberIds )
        {
            this.members.add( member( memberId ) );
        }
    }

    public RaftTestGroup( MemberId... memberIds )
    {
        this.members.addAll( Arrays.asList( memberIds ) );
    }

    @Override
    public Set<MemberId> getMembers()
    {
        return members;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        RaftTestGroup that = (RaftTestGroup) o;

        return members.equals( that.members );

    }

    @Override
    public int hashCode()
    {
        return members.hashCode();
    }

    @Override
    public String toString()
    {
        return format( "RaftTestGroup{members=%s}", members );
    }

    @Override
    public void handle( ReplicatedContentHandler contentHandler )
    {
        throw new UnsupportedOperationException( "No handler for this " + this.getClass() );
    }
}
