/*
 * Copyright (c) 2002-2020 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.kernel.impl.api;

import org.neo4j.kernel.spi.explicitindex.IndexImplementation;

/**
 * Looks up a {@link ExplicitBatchIndexApplier} given a provider name.
 */
public interface ExplicitIndexApplierLookup
{
    TransactionApplier newApplier( String providerName, boolean recovery );

    /**
     * Looks up an {@link IndexImplementation} and calls {@link IndexImplementation#newApplier(boolean)} on it.
     */
    class Direct implements ExplicitIndexApplierLookup
    {
        private final ExplicitIndexProvider provider;

        public Direct( ExplicitIndexProvider provider )
        {
            this.provider = provider;
        }

        @Override
        public TransactionApplier newApplier( String providerName, boolean recovery )
        {
            return provider.getProviderByName( providerName ).newApplier( recovery );
        }
    }
}
