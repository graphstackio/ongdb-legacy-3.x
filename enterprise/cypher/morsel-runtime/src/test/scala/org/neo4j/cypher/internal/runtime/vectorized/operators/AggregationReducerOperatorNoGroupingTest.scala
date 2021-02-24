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
package org.neo4j.cypher.internal.runtime.vectorized.operators

import org.neo4j.cypher.internal.runtime.vectorized.{Morsel, MorselExecutionContext, QueryState}
import org.neo4j.values.AnyValue
import org.neo4j.values.storable.Values
import org.neo4j.cypher.internal.v3_6.util.test_helpers.CypherFunSuite

class AggregationReducerOperatorNoGroupingTest extends CypherFunSuite {

  test("reduce from single morsel") {
    // Given
    val numberOfLongs = 1
    val numberOfReferences = 1
    val aggregation = new AggregationReduceOperatorNoGrouping(Array(AggregationOffsets(0, 0, DummyEvenNodeIdAggregation(0))))
    val refs = new Array[AnyValue](10)
    refs(0) = Values.longArray(Array(2,4,42))
    val in = new Morsel(Array.empty, refs, refs.length)
    val out = new Morsel(new Array[Long](10), new Array[AnyValue](10), refs.length)
    // When
    aggregation.init(null, null, Array(MorselExecutionContext(in, numberOfLongs, numberOfReferences)))
      .operate(MorselExecutionContext(out, numberOfLongs, numberOfReferences), null, QueryState.EMPTY)

    // Then
    out.refs(0) should equal(Values.longArray(Array(2,4,42)))
  }

  test("reduce values from multiple morsels") {
    // Given
    val numberOfLongs = 1
    val numberOfReferences = 1
    val aggregation = new AggregationReduceOperatorNoGrouping(Array(AggregationOffsets(0, 0, DummyEvenNodeIdAggregation(0))))
    val in = 1 to 10 map ( i => {
      val refs = new Array[AnyValue](10)
      refs(0) = Values.longArray(Array(2*i))
      val morsel = new Morsel(Array.empty, refs, refs.length)
      MorselExecutionContext(morsel, numberOfLongs, numberOfReferences)
    })

    val out = new Morsel(new Array[Long](10), new Array[AnyValue](10), 10)

    // When
    aggregation.init(null, null, in).operate(MorselExecutionContext(out, numberOfLongs, numberOfReferences),
                                             null, QueryState.EMPTY)

    // Then
    out.refs(0) should equal(Values.longArray(Array(2,4,6,8,10,12,14,16,18,20)))
  }
}
