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
 * Commons Clause,as found
 * in the associated LICENSE.txt file.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 */
package org.neo4j.internal.cypher.acceptance

import org.neo4j.cypher.internal.RewindableExecutionResult
import org.neo4j.cypher.internal.javacompat.GraphDatabaseCypherService
import org.neo4j.cypher.ExecutionEngineFunSuite
import org.neo4j.cypher.ExecutionEngineHelper
import org.neo4j.cypher.RunWithConfigTestSupport
import org.neo4j.cypher.ShortestPathCommonEndNodesForbiddenException
import org.neo4j.graphdb.RelationshipType
import org.neo4j.graphdb.factory.GraphDatabaseSettings
import org.neo4j.internal.cypher.acceptance.comparisonsupport.Configs
import org.neo4j.internal.cypher.acceptance.comparisonsupport.CypherComparisonSupport
import org.neo4j.internal.cypher.acceptance.comparisonsupport.Planners
import org.neo4j.internal.cypher.acceptance.comparisonsupport.Runtimes
import org.neo4j.internal.cypher.acceptance.comparisonsupport.TestConfiguration
import org.neo4j.internal.cypher.acceptance.comparisonsupport.Versions
import org.neo4j.internal.cypher.acceptance.comparisonsupport.Versions.V3_1
import org.neo4j.internal.cypher.acceptance.comparisonsupport.Versions.V3_4
import org.neo4j.internal.cypher.acceptance.comparisonsupport.Versions.V3_6
import org.neo4j.values.virtual.VirtualValues

class ShortestPathSameNodeAcceptanceTest extends ExecutionEngineFunSuite with RunWithConfigTestSupport with CypherComparisonSupport {

  val expectedToFail = TestConfiguration(
    Versions(V3_1, V3_4, V3_6),
    Planners(Planners.Cost, Planners.Rule),
    Runtimes(Runtimes.Interpreted, Runtimes.Slotted, Runtimes.SlottedWithCompiledExpressions))

  def setupModel(db: GraphDatabaseCypherService) {
    db.inTx {
      val a = db.getGraphDatabaseService.createNode()
      val b = db.getGraphDatabaseService.createNode()
      val c = db.getGraphDatabaseService.createNode()
      a.createRelationshipTo(b, RelationshipType.withName("KNOWS"))
      b.createRelationshipTo(c, RelationshipType.withName("KNOWS"))
    }
  }

  test("shortest paths with explicit same start and end nodes should throw exception by default") {
    setupModel(graph)
    val query = "MATCH p=shortestPath((a)-[*]-(a)) RETURN p"
    failWithError(expectedToFail, query, List("The shortest path algorithm does not work when the start and end nodes are the same."))
  }

  test("shortest paths with explicit same start and end nodes should throw exception when configured to do so") {
    runWithConfig(GraphDatabaseSettings.forbid_shortestpath_common_nodes -> "true") { db =>
      setupModel(db)
      val query = "MATCH p=shortestPath((a)-[*]-(a)) RETURN p"
      intercept[ShortestPathCommonEndNodesForbiddenException](
        executeUsingCostPlannerOnly(db, query).toList
      ).getMessage should include("The shortest path algorithm does not work when the start and end nodes are the same")
    }
  }

  test("shortest paths with explicit same start and end nodes should not throw exception when configured to not do so") {
    runWithConfig(GraphDatabaseSettings.forbid_shortestpath_common_nodes -> "false") { db =>
      setupModel(db)
      val query = "MATCH p=shortestPath((a)-[*]-(a)) RETURN p"
      executeUsingCostPlannerOnly(db, query).toList.length should be(0)
    }
  }

  test("shortest paths that discover at runtime that the start and end nodes are the same should throw exception by default") {
    setupModel(graph)
    val query = "MATCH (a), (b) MATCH p=shortestPath((a)-[*]-(b)) RETURN p"
    failWithError(expectedToFail, query, List("The shortest path algorithm does not work when the start and end nodes are the same."))
  }

  test("shortest paths that discover at runtime that the start and end nodes are the same should throw exception when configured to do so") {
    runWithConfig(GraphDatabaseSettings.forbid_shortestpath_common_nodes -> "true") { db =>
      setupModel(db)
      val query = "MATCH (a), (b) MATCH p=shortestPath((a)-[*]-(b)) RETURN p"
      intercept[ShortestPathCommonEndNodesForbiddenException](
        executeUsingCostPlannerOnly(db, query).toList
      ).getMessage should include("The shortest path algorithm does not work when the start and end nodes are the same")
    }
  }

  test("shortest paths that discover at runtime that the start and end nodes are the same should not throw exception when configured to not do so") {
    runWithConfig(GraphDatabaseSettings.forbid_shortestpath_common_nodes -> "false") { db =>
      setupModel(db)
      val query = "MATCH (a), (b) MATCH p=shortestPath((a)-[*]-(b)) RETURN p"
      executeUsingCostPlannerOnly(db, query).toList.length should be(6)
    }
  }

  test("shortest paths with min length 0 that discover at runtime that the start and end nodes are the same should not throw exception by default") {
    setupModel(graph)
    val query = "MATCH (a), (b) MATCH p=shortestPath((a)-[*0..]-(b)) RETURN p"
    executeWith(Configs.InterpretedAndSlotted, query).toList.length should be(9)
  }

  test("shortest paths with min length 0 that discover at runtime that the start and end nodes are the same should throw exception even when when configured to do so") {
    runWithConfig(GraphDatabaseSettings.forbid_shortestpath_common_nodes -> "true") { db =>
      setupModel(db)
      val query = "MATCH (a), (b) MATCH p=shortestPath((a)-[*0..]-(b)) RETURN p"
      executeUsingCostPlannerOnly(db, query).toList.length should be(9)
    }
  }

  test("shortest paths with min length 0 that discover at runtime that the start and end nodes are the same should not throw exception when configured to not do so") {
    runWithConfig(GraphDatabaseSettings.forbid_shortestpath_common_nodes -> "false") { db =>
      setupModel(db)
      val query = "MATCH (a), (b) MATCH p=shortestPath((a)-[*0..]-(b)) RETURN p"
      executeUsingCostPlannerOnly(db, query).toList.length should be(9)
    }
  }

  def executeUsingCostPlannerOnly(db: GraphDatabaseCypherService, query: String): RewindableExecutionResult = {
    val engine = ExecutionEngineHelper.createEngine(db)
    RewindableExecutionResult(engine.execute(query,
                                             VirtualValues.emptyMap(),
                                             engine.queryService.transactionalContext(query = query -> Map())))
  }
}
