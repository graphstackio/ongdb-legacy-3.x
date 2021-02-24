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
package org.neo4j.causalclustering.diagnostics;

import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.UUID;

import org.neo4j.causalclustering.identity.ClusterBinder;
import org.neo4j.causalclustering.identity.ClusterId;
import org.neo4j.kernel.monitoring.Monitors;
import org.neo4j.logging.AssertableLogProvider;
import org.neo4j.logging.internal.SimpleLogService;

public class CoreMonitorTest
{

    @Test
    public void shouldNotDuplicateToAnyLog()
    {
        AssertableLogProvider userLogProvider = new AssertableLogProvider();
        AssertableLogProvider debugLogProvider = new AssertableLogProvider();

        SimpleLogService logService = new SimpleLogService( userLogProvider, debugLogProvider );

        Monitors monitors = new Monitors();
        CoreMonitor.register( logService.getInternalLogProvider(), logService.getUserLogProvider(), monitors );

        ClusterBinder.Monitor monitor = monitors.newMonitor( ClusterBinder.Monitor.class );

        ClusterId clusterId = new ClusterId( UUID.randomUUID() );
        monitor.boundToCluster( clusterId );

        userLogProvider.rawMessageMatcher().assertContainsSingle(
                Matchers.allOf(
                        Matchers.containsString( "Bound to cluster with id " + clusterId.uuid() )
                )
        );

        debugLogProvider.rawMessageMatcher().assertContainsSingle(
                Matchers.allOf(
                        Matchers.containsString( "Bound to cluster with id " + clusterId.uuid() )
                )
        );
    }
}
