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
package org.neo4j.procedure;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.QueryExecutionException;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.kernel.configuration.Settings;
import org.neo4j.kernel.impl.enterprise.configuration.OnlineBackupSettings;
import org.neo4j.helpers.collection.Iterators;
import org.neo4j.kernel.impl.proc.JarBuilder;
import org.neo4j.logging.Log;
import org.neo4j.test.TestGraphDatabaseFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.neo4j.helpers.collection.MapUtil.map;

public class UserAggregationFunctionIT
{
    @Rule
    public TemporaryFolder plugins = new TemporaryFolder();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private GraphDatabaseService db;

    @Test
    public void shouldHandleSingleStringArgumentAggregationFunction()
    {
        // Given
        try ( Transaction tx = db.beginTx() )
        {
            db.execute( "CREATE ({ prop:'foo'})" );
            db.execute( "CREATE ({ prop:'foo'})" );
            db.execute( "CREATE ({ prop:'bar'})" );
            db.execute( "CREATE ({prop:'baz'})" );
            db.execute( "CREATE ()" );
            tx.success();
        }

        // When
        Result result = db.execute( "MATCH (n) RETURN org.neo4j.procedure.count(n.prop) AS count" );

        // Then
        assertThat( result.next(), equalTo( map( "count", 4L ) ) );
        assertFalse( result.hasNext() );
    }

    @Test
    public void shouldHandleSingleStringArgumentAggregationFunctionAndGroupingKey()
    {
        // Given
        try ( Transaction tx = db.beginTx() )
        {
            db.execute( "CREATE ({prop1:42, prop2:'foo'})" );
            db.execute( "CREATE ({prop1:42, prop2:'foo'})" );
            db.execute( "CREATE ({prop1:42, prop2:'bar'})" );
            db.execute( "CREATE ({prop1:1337, prop2:'baz'})" );
            db.execute( "CREATE ({prop1:1337})" );
            tx.success();
        }

        // When
        Result result = db.execute( "MATCH (n) RETURN n.prop1, org.neo4j.procedure.count(n.prop2) AS count" );

        // Then
        assertThat( result.next(), equalTo( map( "n.prop1", 42L, "count", 3L ) ) );
        assertThat( result.next(), equalTo( map( "n.prop1", 1337L, "count", 1L ) ) );
        assertFalse( result.hasNext() );
    }

    @Test
    public void shouldFailNicelyWhenInvalidRuntimeType()
    {
        // Given
        try ( Transaction tx = db.beginTx() )
        {
            db.execute( "CREATE ({ prop:'foo'})" );
            db.execute( "CREATE ({ prop:'foo'})" );
            db.execute( "CREATE ({ prop:'bar'})" );
            db.execute( "CREATE ({prop:42})" );
            db.execute( "CREATE ()" );
            tx.success();
        }

        // Expect
        exception.expect( QueryExecutionException.class );
        exception.expectMessage( "Can't coerce `Long(42)` to String" );

        // When
        db.execute( "MATCH (n) RETURN org.neo4j.procedure.count(n.prop) AS count" );
    }

    @Test
    public void shouldHandleNodeArgumentAggregationFunction()
    {
        // Given
        try ( Transaction tx = db.beginTx() )
        {
            db.execute( "CREATE ({ level:42})" );
            db.execute( "CREATE ({ level:1337})" );
            db.execute( "CREATE ({ level:0})" );
            db.execute( "CREATE ()" );
            tx.success();
        }

        // When
        Result result =
                db.execute( "MATCH (n) WITH org.neo4j.procedure.findBestNode(n) AS best RETURN best.level AS level" );

        // Then
        assertThat( result.next(), equalTo( map( "level", 1337L ) ) );
        assertFalse( result.hasNext() );
    }

    @Test
    public void shouldHandleRelationshipArgumentAggregationFunction()
    {
        // Given
        try ( Transaction tx = db.beginTx() )
        {
            db.execute( "CREATE ()-[:T {level:42}]->()" );
            db.execute( "CREATE ()-[:T {level:1337}]->()" );
            db.execute( "CREATE ()-[:T {level:2}]->()" );
            db.execute( "CREATE ()-[:T]->()" );
            tx.success();
        }

        // When
        Result result = db.execute(
                "MATCH ()-[r]->() WITH org.neo4j.procedure.findBestRel(r) AS best RETURN best.level AS level" );

        // Then
        assertThat( result.next(), equalTo( map( "level", 1337L ) ) );
        assertFalse( result.hasNext() );
    }

    @Test
    public void shouldHandlePathArgumentAggregationFunction()
    {
        // Given
        try ( Transaction tx = db.beginTx() )
        {
            db.execute( "CREATE ()-[:T]->()" );
            db.execute( "CREATE ()-[:T]->()-[:T]->()" );
            db.execute( "CREATE ()-[:T]->()-[:T]->()-[:T]->()" );
            tx.success();
        }

        // When
        Result result = db.execute(
                "MATCH p=()-[:T*]->() WITH org.neo4j.procedure.longestPath(p) AS longest RETURN length(longest) AS " +
                "len" );

        // Then
        assertThat( result.next(), equalTo( map( "len", 3L ) ) );
        assertFalse( result.hasNext() );
    }

    @Test
    public void shouldHandleNullPath()
    {
        // When
        Result result = db.execute(
                "MATCH p=()-[:T*]->() WITH org.neo4j.procedure.longestPath(p) AS longest RETURN longest");

        // Then
        assertThat( result.next(), equalTo( map( "longest", null ) ) );
        assertFalse( result.hasNext() );
    }

    @Test
    public void shouldHandleNumberArgumentAggregationFunction()
    {
        // Given, When
        Result result = db.execute(
                "UNWIND [43, 42.5, 41.9, 1337] AS num RETURN org.neo4j.procedure.near42(num) AS closest" );

        // Then
        assertThat( result.next(), equalTo( map( "closest", 41.9 ) ) );
        assertFalse( result.hasNext() );
    }

    @Test
    public void shouldHandleDoubleArgumentAggregationFunction()
    {
        // Given, When
        Result result = db.execute(
                "UNWIND [43, 42.5, 41.9, 1337] AS num RETURN org.neo4j.procedure.doubleAggregator(num) AS closest" );

        // Then
        assertThat( result.next(), equalTo( map( "closest", 41.9 ) ) );
        assertFalse( result.hasNext() );
    }

    @Test
    public void shouldHandleLongArgumentAggregationFunction()
    {
        // Given, When
        Result result = db.execute(
                "UNWIND [43, 42.5, 41.9, 1337] AS num RETURN org.neo4j.procedure.longAggregator(num) AS closest" );

        // Then
        assertThat( result.next(), equalTo( map( "closest", 42L ) ) );
        assertFalse( result.hasNext() );
    }

    @Test
    public void shouldHandleNoArgumentBooleanAggregationFunction()
    {
        assertThat( db.execute(
                "UNWIND [1,2] AS num RETURN org.neo4j.procedure.boolAggregator() AS wasCalled" ).next(),
                equalTo( map( "wasCalled", true ) ) );
        assertThat( db.execute(
                "UNWIND [] AS num RETURN org.neo4j.procedure.boolAggregator() AS wasCalled" ).next(),
                equalTo( map( "wasCalled", false ) ) );

    }

    @Test
    public void shouldBeAbleToUseAdbInFunction()
    {
        List<Node> nodes = new ArrayList<>();
        // Given
        try ( Transaction tx = db.beginTx() )
        {
            nodes.add( db.createNode() );
            nodes.add( db.createNode() );
            nodes.add( db.createNode() );
            nodes.add( db.createNode() );
            tx.success();
        }

        // When
        Result result = db.execute(
                "UNWIND $ids AS ids WITH org.neo4j.procedure.collectNode(ids) AS nodes RETURN nodes",
                map("ids", nodes.stream().map( Node::getId ).collect( Collectors.toList() )));

        // Then
        assertThat( result.next(), equalTo( map( "nodes", nodes ) ) );
        assertFalse( result.hasNext() );
    }

    //TODO unignore when we have updated front end dependency
    @Ignore
    public void shouldBeAbleToAccessPropertiesFromAggregatedValues()
    {
        // Given
        try ( Transaction tx = db.beginTx() )
        {
            db.execute( "CREATE (:User {country: 'Sweden'})" );
            db.execute( "CREATE (:User {country: 'Sweden'})" );
            db.execute( "CREATE (:User {country: 'Sweden'})" );
            db.execute( "CREATE (:User {country: 'Sweden'})" );
            db.execute( "CREATE (:User {country: 'Germany'})" );
            db.execute( "CREATE (:User {country: 'Germany'})" );
            db.execute( "CREATE (:User {country: 'Germany'})" );
            db.execute( "CREATE (:User {country: 'Mexico'})" );
            db.execute( "CREATE (:User {country: 'Mexico'})" );
            db.execute( "CREATE (:User {country: 'South Korea'})" );
            tx.success();
        }

        // When
        List<Map<String,Object>> result =
                Iterators.asList( db.execute(
                        "MATCH (u:User) RETURN u.country,count(*),org.neo4j.procedure.first(u).country AS first" ) );

        // Then
        assertThat( result, hasSize( 4 ) );
    }

    @Before
    public void setUp() throws IOException
    {
        new JarBuilder().createJarFor( plugins.newFile( "myFunctions.jar" ), ClassWithFunctions.class );
        db = new TestGraphDatabaseFactory()
                .newImpermanentDatabaseBuilder()
                .setConfig( GraphDatabaseSettings.plugin_dir, plugins.getRoot().getAbsolutePath() )
                .setConfig( OnlineBackupSettings.online_backup_enabled, Settings.FALSE )
                .newGraphDatabase();

    }

    @After
    public void tearDown()
    {
        if ( this.db != null )
        {
            this.db.shutdown();
        }
    }

    public static class ClassWithFunctions
    {
        @Context
        public GraphDatabaseService db;

        @Context
        public Log log;

        @UserAggregationFunction
        public CountAggregator count()
        {
            return new CountAggregator();
        }

        @UserAggregationFunction
        public RelAggregator findBestRel()
        {
            return new RelAggregator();
        }

        @UserAggregationFunction
        public LongestPathAggregator longestPath()
        {
            return new LongestPathAggregator();
        }

        @UserAggregationFunction
        public NodeAggregator findBestNode()
        {
            return new NodeAggregator();
        }

        @UserAggregationFunction
        public DoubleAggregator doubleAggregator()
        {
            return new DoubleAggregator();
        }

        @UserAggregationFunction
        public LongAggregator longAggregator()
        {
            return new LongAggregator();
        }

        @UserAggregationFunction
        public BoolAggregator boolAggregator()
        {
            return new BoolAggregator();
        }

        @UserAggregationFunction
        public ClosestTo42Aggregator near42()
        {
            return new ClosestTo42Aggregator();
        }

        @UserAggregationFunction
        public NodeFromIdAggregator collectNode()
        {
            return new NodeFromIdAggregator( db );
        }

        @UserAggregationFunction
        public FirstAggregator first()
        {
            return new FirstAggregator();
        }

        public static class FirstAggregator
        {
            private Object first;

            @UserAggregationUpdate
            public void update( @Name( "item" ) Object o )
            {
                if ( first == null )
                {
                    first = o;
                }
            }

            @UserAggregationResult
            public Object result()
            {
                return first;
            }
        }

        public static class NodeAggregator
        {
            private Node aggregateNode;

            @UserAggregationUpdate
            public void update( @Name( "node" ) Node node )
            {
                if ( node != null )
                {
                    long level = (long) node.getProperty( "level", 0L );

                    if ( aggregateNode == null )
                    {
                        aggregateNode = node;
                    }
                    else if ( level > (long) aggregateNode.getProperty( "level", 0L ) )
                    {
                        aggregateNode = node;
                    }
                }
            }

            @UserAggregationResult
            public Node result()
            {
                return aggregateNode;
            }
        }

        public static class RelAggregator
        {
            private Relationship aggregateRel;

            @UserAggregationUpdate
            public void update( @Name( "rel" ) Relationship rel )
            {
                if ( rel != null )
                {
                    long level = (long) rel.getProperty( "level", 0L );

                    if ( aggregateRel == null )
                    {
                        aggregateRel = rel;
                    }
                    else if ( level > (long) aggregateRel.getProperty( "level", 0L ) )
                    {
                        aggregateRel = rel;
                    }
                }
            }

            @UserAggregationResult
            public Relationship result()
            {
                return aggregateRel;
            }
        }

        public static class LongestPathAggregator
        {
            private Path aggregatePath;
            private int longest;

            @UserAggregationUpdate
            public void update( @Name( "path" ) Path path )
            {
                if ( path != null )
                {
                    if ( path.length() > longest )
                    {
                        longest = path.length();
                        aggregatePath = path;
                    }
                }
            }

            @UserAggregationResult
            public Path result()
            {
                return aggregatePath;
            }
        }

        public static class ClosestTo42Aggregator
        {
            private Number closest;

            @UserAggregationUpdate
            public void update( @Name( "number" ) Number number )
            {
                if ( number != null )
                {
                    if ( closest == null )
                    {
                        closest = number;
                    }
                    else if ( Math.abs( number.doubleValue() - 42L ) < Math.abs( closest.doubleValue() - 42L ) )
                    {
                        closest = number;
                    }
                }
            }

            @UserAggregationResult
            public Number result()
            {
                return closest;
            }
        }

        public static class DoubleAggregator
        {
            private Double closest;

            @UserAggregationUpdate
            public void update( @Name( "double" ) Double number )
            {
                if ( number != null )
                {
                    if ( closest == null )
                    {
                        closest = number;
                    }
                    else if ( Math.abs( number - 42L ) < Math.abs( closest - 42L ) )
                    {
                        closest = number;
                    }
                }
            }

            @UserAggregationResult
            public Double result()
            {
                return closest;
            }
        }

        public static class LongAggregator
        {
            private Long closest;

            @UserAggregationUpdate
            public void update( @Name( "long" ) Long number )
            {
                if ( number != null )
                {
                    if ( closest == null )
                    {
                        closest = number;
                    }
                    else if ( Math.abs( number - 42L ) < Math.abs( closest - 42L ) )
                    {
                        closest = number;
                    }
                }
            }

            @UserAggregationResult
            public Long result()
            {
                return closest;
            }
        }

        public static class CountAggregator
        {
            private long count;

            @UserAggregationUpdate
            public void update( @Name( "in" ) String in )
            {
                if ( in != null )
                {
                    count += 1L;
                }
            }

            @UserAggregationResult
            public long result()
            {
                return count;
            }
        }

        public static class BoolAggregator
        {
            private boolean wasCalled;

            @UserAggregationUpdate
            public void update()
            {
                wasCalled = true;
            }

            @UserAggregationResult
            public boolean result()
            {
                return wasCalled;
            }
        }

        public static class NodeFromIdAggregator
        {
            private final List<Long> ids = new ArrayList<>();
            private final GraphDatabaseService gds;

            public NodeFromIdAggregator( GraphDatabaseService gds )
            {
                this.gds = gds;
            }

            @UserAggregationUpdate
            public void update( @Name( "id" ) long id )
            {
                ids.add( id );
            }

            @UserAggregationResult
            public List<Node> result()
            {
                return ids.stream().map( gds::getNodeById ).collect( Collectors.toList() );
            }

        }
    }
}
