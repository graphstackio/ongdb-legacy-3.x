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
package org.neo4j.causalclustering.stresstests;

import org.neo4j.helper.Workload;

enum Workloads
{
    CreateNodesWithProperties
            {
                @Override
                Workload create( Control control, Resources resources, Config config )
                {
                    return new CreateNodesWithProperties( control, resources, config );
                }
            },
    StartStopRandomMember
            {
                @Override
                Workload create( Control control, Resources resources, Config config )
                {
                    return new StartStopRandomMember( control, resources );
                }
            },
    StartStopRandomCore
            {
                @Override
                Workload create( Control control, Resources resources, Config config )
                {
                    return new StartStopRandomCore( control, resources );
                }
            },
    BackupRandomMember
            {
                @Override
                Workload create( Control control, Resources resources, Config config )
                {
                    return new BackupRandomMember( control, resources );
                }
            },
    CatchupNewReadReplica
            {
                @Override
                Workload create( Control control, Resources resources, Config config )
                {
                    return new CatchupNewReadReplica( control, resources );
                }
            },
    ReplaceRandomMember
            {
                @Override
                Workload create( Control control, Resources resources, Config config )
                {
                    return new ReplaceRandomMember( control, resources );
                }
            },
    IdReuseInsertion
            {
                @Override
                Workload create( Control control, Resources resources, Config config )
                {
                    return new IdReuse.InsertionWorkload( control, resources );
                }
            },
    IdReuseDeletion
            {
                @Override
                Workload create( Control control, Resources resources, Config config )
                {
                    return new IdReuse.DeletionWorkload( control, resources );
                }
            },
    IdReuseReelection
            {
                @Override
                Workload create( Control control, Resources resources, Config config )
                {
                    return new IdReuse.ReelectionWorkload( control, resources, config );
                }
            },
    FailingWorkload
            {
                @Override
                Workload create( Control control, Resources resources, Config config )
                {
                    return new FailingWorkload( control );
                }
            };

    abstract Workload create( Control control, Resources resources, Config config );
}
