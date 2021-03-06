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
package org.neo4j.causalclustering.core.state.machines.token;

import java.util.function.Supplier;

import org.neo4j.causalclustering.core.replication.RaftReplicator;
import org.neo4j.kernel.api.txstate.TransactionState;
import org.neo4j.kernel.impl.core.TokenRegistry;
import org.neo4j.kernel.impl.store.id.IdGeneratorFactory;
import org.neo4j.storageengine.api.StorageEngine;

import static org.neo4j.causalclustering.core.state.machines.token.TokenType.RELATIONSHIP;
import static org.neo4j.kernel.impl.store.id.IdType.RELATIONSHIP_TYPE_TOKEN;

public class ReplicatedRelationshipTypeTokenHolder extends ReplicatedTokenHolder
{
    public ReplicatedRelationshipTypeTokenHolder( TokenRegistry registry, RaftReplicator replicator, IdGeneratorFactory idGeneratorFactory,
            Supplier<StorageEngine> storageEngineSupplier )
    {
        super( registry, replicator, idGeneratorFactory, RELATIONSHIP_TYPE_TOKEN, storageEngineSupplier, RELATIONSHIP,
                TransactionState::relationshipTypeDoCreateForName );
    }
}
