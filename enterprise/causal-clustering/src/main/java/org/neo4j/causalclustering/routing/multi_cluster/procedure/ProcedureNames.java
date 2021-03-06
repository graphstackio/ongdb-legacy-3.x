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
package org.neo4j.causalclustering.routing.multi_cluster.procedure;

import org.neo4j.causalclustering.routing.procedure.ProcedureNamesEnum;

/**
 * This is part of the cluster / driver interface specification and
 * defines the procedure names involved in the load balancing solution.
 *
 * These procedures are used by cluster driver software to discover endpoints,
 * their capabilities and to eventually schedule work appropriately.
 *
 * The intention is for this class to eventually move over to a support package
 * which can be included by driver software.
 */
public enum ProcedureNames implements ProcedureNamesEnum
{
    GET_ROUTERS_FOR_DATABASE( "getRoutersForDatabase" ),
    GET_ROUTERS_FOR_ALL_DATABASES( "getRoutersForAllDatabases" );

    private static final String[] nameSpace = new String[]{"dbms", "cluster", "routing"};
    private final String name;

    ProcedureNames( String name )
    {
        this.name = name;
    }

    @Override
    public String procedureName()
    {
        return name;
    }

    @Override
    public String[] procedureNameSpace()
    {
        return nameSpace;
    }

}
