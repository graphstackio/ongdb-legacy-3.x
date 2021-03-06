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
package org.neo4j.causalclustering.discovery;

import org.junit.Test;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.LongConsumer;
import java.util.function.Predicate;

import org.neo4j.helpers.AdvertisedSocketAddress;
import org.neo4j.logging.NullLogProvider;

import static org.junit.Assert.assertEquals;

public class MultiRetryStrategyTest
{
    private static final Predicate<Integer> ALWAYS_VALID = i -> true;
    private static final Predicate<Integer> NEVER_VALID = i -> false;
    private static final Predicate<Integer> VALID_ON_SECOND_TIME = new Predicate<Integer>()
    {
        private boolean nextSuccessful;
        @Override
        public boolean test( Integer integer )
        {
            if ( !nextSuccessful )
            {
                nextSuccessful = true;
                return false;
            }
            return true;
        }
    };

    @Test
    public void successOnRetryCausesNoDelay()
    {
        // given
        CountingSleeper countingSleeper = new CountingSleeper();
        int retries = 10;
        MultiRetryStrategy<Integer,Integer> subject = new MultiRetryStrategy<>( 0, retries, NullLogProvider.getInstance(), countingSleeper );

        // when
        Integer result = subject.apply( 3, Function.identity(), ALWAYS_VALID );

        // then
        assertEquals( 0, countingSleeper.invocationCount() );
        assertEquals( "Function identity should be used to retrieve the expected value", 3, result.intValue() );
    }

    @Test
    public void numberOfIterationsDoesNotExceedMaximum()
    {
        // given
        CountingSleeper countingSleeper = new CountingSleeper();
        int retries = 5;
        MultiRetryStrategy<Integer,Integer> subject = new MultiRetryStrategy<>( 0, retries, NullLogProvider.getInstance(), countingSleeper );

        // when
        subject.apply( 3, Function.identity(), NEVER_VALID );

        // then
        assertEquals( retries, countingSleeper.invocationCount() );
    }

    @Test
    public void successfulRetriesBreakTheRetryLoop()
    {
        CountingSleeper countingSleeper = new CountingSleeper();
        int retries = 5;
        MultiRetryStrategy<Integer,Integer> subject = new MultiRetryStrategy<>( 0, retries, NullLogProvider.getInstance(), countingSleeper );

        // when
        subject.apply( 3, Function.identity(), VALID_ON_SECOND_TIME );

        // then
        assertEquals( 1, countingSleeper.invocationCount() );
    }

    public static class CountingSleeper implements LongConsumer
    {
        private int counter;

        @Override
        public void accept( long l )
        {
            counter++;
        }

        public int invocationCount()
        {
            return counter;
        }

    }

    public static MultiRetryStrategy<AdvertisedSocketAddress,Collection<AdvertisedSocketAddress>> testRetryStrategy( int numRetries )
    {
        return new MultiRetryStrategy<>( 0, numRetries, NullLogProvider.getInstance(), new CountingSleeper() );
    }
}
