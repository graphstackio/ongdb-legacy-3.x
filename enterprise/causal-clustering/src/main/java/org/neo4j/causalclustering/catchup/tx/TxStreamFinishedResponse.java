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
package org.neo4j.causalclustering.catchup.tx;

import java.util.Objects;

import org.neo4j.causalclustering.catchup.CatchupResult;

public class TxStreamFinishedResponse
{
    private final CatchupResult status;
    private final long latestTxId;

    public CatchupResult status()
    {
        return status;
    }

    TxStreamFinishedResponse( CatchupResult status, long latestTxId )
    {
        this.status = status;
        this.latestTxId = latestTxId;
    }

    public long latestTxId()
    {
        return latestTxId;
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
        TxStreamFinishedResponse that = (TxStreamFinishedResponse) o;
        return latestTxId == that.latestTxId && status == that.status;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( status, latestTxId );
    }

    @Override
    public String toString()
    {
        return "TxStreamFinishedResponse{" +
               "status=" + status +
               ", latestTxId=" + latestTxId +
               '}';
    }
}
