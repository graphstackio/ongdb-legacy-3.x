/*
 * Copyright (c) 2002-2020 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.cypher.internal.v3_5.rewriting.conditions

import org.neo4j.cypher.internal.v3_5.util.ASTNode
import org.neo4j.cypher.internal.v3_5.util.Foldable._

import scala.reflect.ClassTag

case class collectNodesOfType[T <: ASTNode](implicit tag: ClassTag[T]) extends (Any => Seq[T]) {
  def apply(that: Any): Seq[T] = that.fold(Seq.empty[T]) {
    case node: ASTNode if node.getClass == tag.runtimeClass =>
      (acc) => acc :+ node.asInstanceOf[T]
  }
}
