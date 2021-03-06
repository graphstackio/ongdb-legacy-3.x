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

import org.neo4j.cypher.internal.runtime.interpreted.pipes.IndexSeekByRange
import org.neo4j.cypher.{ExecutionEngineFunSuite, QueryStatisticsTestSupport}
import org.neo4j.graphdb.{Node, ResourceIterator}
import org.neo4j.internal.cypher.acceptance.comparisonsupport.CypherComparisonSupport
import org.neo4j.internal.cypher.acceptance.comparisonsupport.Configs

class StartsWithImplementationAcceptanceTest extends ExecutionEngineFunSuite with QueryStatisticsTestSupport with CypherComparisonSupport {

  var aNode: Node = null
  var bNode: Node = null
  var cNode: Node = null
  var dNode: Node = null
  var eNode: Node = null
  var fNode: Node = null

  override def initTest() {
    super.initTest()
    aNode = createLabeledNode(Map("name" -> "ABCDEF"), "LABEL")
    bNode = createLabeledNode(Map("name" -> "AB"), "LABEL")
    cNode = createLabeledNode(Map("name" -> "abcdef"), "LABEL")
    dNode = createLabeledNode(Map("name" -> "ab"), "LABEL")
    eNode = createLabeledNode(Map("name" -> ""), "LABEL")
    fNode = createLabeledNode("LABEL")
  }

  test("should not plan an IndexSeek when index doesn't exist") {

    (1 to 10).foreach { _ =>
      createLabeledNode("Address")
    }

    createLabeledNode(Map("prop" -> "www123"), "Address")
    createLabeledNode(Map("prop" -> "www"), "Address")

    val result = executeWith(Configs.InterpretedAndSlotted, "MATCH (a:Address) WHERE a.prop STARTS WITH 'www' RETURN a")

    result.executionPlanDescription() should not(includeSomewhere.aPlan(IndexSeekByRange.name))
  }

  test("Should handle prefix search with existing transaction state") {
    graph.createIndex("User", "name")
    graph.inTx {
      createLabeledNode(Map("name" -> "Stefan"), "User")
      createLabeledNode(Map("name" -> "Stephan"), "User")
      createLabeledNode(Map("name" -> "Stefanie"), "User")
      createLabeledNode(Map("name" -> "Craig"), "User")
    }
      val executeBefore = () => {
        drain(graph.execute("MATCH (u:User {name: 'Craig'}) SET u.name = 'Steven'"))
        drain(graph.execute("MATCH (u:User {name: 'Stephan'}) DELETE u"))
        drain(graph.execute("MATCH (u:User {name: 'Stefanie'}) SET u.name = 'steffi'"))
      }

      executeWith(Configs.InterpretedAndSlotted, "MATCH (u:User) WHERE u.name STARTS WITH 'Ste' RETURN u.name as name", executeBefore = executeBefore,
        resultAssertionInTx = Some(result => {
          result.toSet should equal(Set(Map("name" -> "Stefan"),Map("name" -> "Steven")))
        }))
  }

  private def drain(iter: ResourceIterator[_]): Unit = {
    try {
      while (iter.hasNext) iter.next()
    } finally {
      iter.close()
    }
  }
}
