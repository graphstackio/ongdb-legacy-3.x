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
package org.neo4j.kernel.impl.store.format.highlimit.v320;

import org.neo4j.helpers.Service;
import org.neo4j.kernel.impl.store.format.RecordFormats;

@Service.Implementation( RecordFormats.Factory.class )
public class HighLimitFactoryV3_2_0 extends RecordFormats.Factory
{
    public HighLimitFactoryV3_2_0()
    {
        super( HighLimitV3_2_0.NAME, HighLimitV3_2_0.STORE_VERSION );
    }

    @Override
    public RecordFormats newInstance()
    {
        return HighLimitV3_2_0.RECORD_FORMATS;
    }
}
