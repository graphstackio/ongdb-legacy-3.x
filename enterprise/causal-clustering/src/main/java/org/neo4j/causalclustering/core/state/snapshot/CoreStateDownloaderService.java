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
package org.neo4j.causalclustering.core.state.snapshot;

import java.util.Optional;
import java.util.function.Supplier;

import org.neo4j.causalclustering.catchup.CatchupAddressProvider;
import org.neo4j.causalclustering.core.state.CommandApplicationProcess;
import org.neo4j.causalclustering.helper.TimeoutStrategy;
import org.neo4j.kernel.internal.DatabaseHealth;
import org.neo4j.kernel.lifecycle.LifecycleAdapter;
import org.neo4j.kernel.monitoring.Monitors;
import org.neo4j.logging.Log;
import org.neo4j.logging.LogProvider;
import org.neo4j.scheduler.Group;
import org.neo4j.scheduler.JobScheduler;
import org.neo4j.scheduler.JobHandle;

public class CoreStateDownloaderService extends LifecycleAdapter
{
    private final JobScheduler jobScheduler;
    private final CoreStateDownloader downloader;
    private final CommandApplicationProcess applicationProcess;
    private final Log log;
    private final TimeoutStrategy.Timeout downloaderPauseStrategy;
    private PersistentSnapshotDownloader currentJob;
    private JobHandle jobHandle;
    private boolean stopped;
    private Supplier<DatabaseHealth> dbHealth;
    private final Monitors monitors;

    public CoreStateDownloaderService( JobScheduler jobScheduler, CoreStateDownloader downloader,
            CommandApplicationProcess applicationProcess, LogProvider logProvider,
            TimeoutStrategy.Timeout downloaderPauseStrategy, Supplier<DatabaseHealth> dbHealth, Monitors monitors )
    {
        this.jobScheduler = jobScheduler;
        this.downloader = downloader;
        this.applicationProcess = applicationProcess;
        this.log = logProvider.getLog( getClass() );
        this.downloaderPauseStrategy = downloaderPauseStrategy;
        this.dbHealth = dbHealth;
        this.monitors = monitors;
    }

    public synchronized Optional<JobHandle> scheduleDownload( CatchupAddressProvider addressProvider )
    {
        if ( stopped )
        {
            return Optional.empty();
        }

        if ( currentJob == null || currentJob.hasCompleted() )
        {
            currentJob = new PersistentSnapshotDownloader( addressProvider, applicationProcess, downloader, log,
                    downloaderPauseStrategy, dbHealth, monitors );
            jobHandle = jobScheduler.schedule( Group.DOWNLOAD_SNAPSHOT, currentJob );
            return Optional.of( jobHandle );
        }
        return Optional.of( jobHandle );
    }

    @Override
    public synchronized void stop() throws Throwable
    {
        stopped = true;

        if ( currentJob != null )
        {
            currentJob.stop();
        }
    }

    public synchronized Optional<JobHandle> downloadJob()
    {
        return Optional.ofNullable( jobHandle );
    }
}
