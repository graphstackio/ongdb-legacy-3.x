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
package org.neo4j.kernel.ha.lock;

import org.junit.Test;

import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.locking.Locks;
import org.neo4j.kernel.impl.locking.StatementLocks;
import org.neo4j.kernel.impl.locking.StatementLocksFactory;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SlaveStatementLocksFactoryTest
{

    @Test
    public void createSlaveStatementLocks()
    {
        StatementLocksFactory delegate = mock( StatementLocksFactory.class );
        Locks locks = mock( Locks.class );
        Config config = Config.defaults();

        SlaveStatementLocksFactory slaveStatementLocksFactory = new SlaveStatementLocksFactory( delegate );
        slaveStatementLocksFactory.initialize( locks, config );
        StatementLocks statementLocks = slaveStatementLocksFactory.newInstance();

        assertThat( statementLocks, instanceOf( SlaveStatementLocks.class ) );
        verify( delegate ).initialize( locks, config );
        verify( delegate ).newInstance();
    }
}
