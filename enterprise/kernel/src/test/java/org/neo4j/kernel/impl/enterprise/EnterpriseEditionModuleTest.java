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
package org.neo4j.kernel.impl.enterprise;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.function.Predicate;

import org.neo4j.io.layout.DatabaseLayout;
import org.neo4j.kernel.impl.index.IndexConfigStore;
import org.neo4j.kernel.impl.pagecache.PageCacheWarmer;
import org.neo4j.kernel.impl.transaction.log.files.TransactionLogFiles;
import org.neo4j.test.extension.Inject;
import org.neo4j.test.extension.TestDirectoryExtension;
import org.neo4j.test.rule.TestDirectory;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith( TestDirectoryExtension.class )
class EnterpriseEditionModuleTest
{
    @Inject
    private TestDirectory testDirectory;

    @Test
    void fileWatcherFileNameFilter()
    {
        DatabaseLayout layout = testDirectory.databaseLayout();
        Predicate<String> filter = EnterpriseEditionModule.enterpriseNonClusterFileWatcherFileNameFilter();
        String metadataStoreName = layout.metadataStore().getName();

        assertFalse( filter.test( metadataStoreName ) );
        assertFalse( filter.test( layout.nodeStore().getName() ) );
        assertTrue( filter.test( TransactionLogFiles.DEFAULT_NAME + ".1" ) );
        assertTrue( filter.test( IndexConfigStore.INDEX_DB_FILE_NAME + ".any" ) );
        assertTrue( filter.test( metadataStoreName + PageCacheWarmer.SUFFIX_CACHEPROF ) );
    }
}
