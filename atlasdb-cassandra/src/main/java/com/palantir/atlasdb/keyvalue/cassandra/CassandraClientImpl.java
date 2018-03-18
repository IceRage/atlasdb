/*
 * Copyright 2017 Palantir Technologies, Inc. All rights reserved.
 *
 * Licensed under the BSD-3 License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.atlasdb.keyvalue.cassandra;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.cassandra.thrift.CASResult;
import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.CfDef;
import org.apache.cassandra.thrift.Column;
import org.apache.cassandra.thrift.ColumnOrSuperColumn;
import org.apache.cassandra.thrift.ColumnParent;
import org.apache.cassandra.thrift.ColumnPath;
import org.apache.cassandra.thrift.Compression;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.CqlPreparedResult;
import org.apache.cassandra.thrift.CqlResult;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.cassandra.thrift.KeyRange;
import org.apache.cassandra.thrift.KeySlice;
import org.apache.cassandra.thrift.KsDef;
import org.apache.cassandra.thrift.Mutation;
import org.apache.cassandra.thrift.NotFoundException;
import org.apache.cassandra.thrift.SchemaDisagreementException;
import org.apache.cassandra.thrift.SlicePredicate;
import org.apache.cassandra.thrift.TimedOutException;
import org.apache.cassandra.thrift.TokenRange;
import org.apache.cassandra.thrift.UnavailableException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.transport.TTransportException;

import com.google.common.collect.ImmutableSet;
import com.palantir.atlasdb.keyvalue.api.TableReference;
import com.palantir.atlasdb.keyvalue.impl.AbstractKeyValueService;
import com.palantir.common.base.Throwables;

@SuppressWarnings({"all"}) // thrift variable names.
public class CassandraClientImpl implements CassandraClient {
    private final Cassandra.Client client;
    private boolean isValid;

    private static final Set<Class> blackListedExceptions =
            ImmutableSet.of(TTransportException.class,  TProtocolException.class, NoSuchElementException.class);

    public CassandraClientImpl(Cassandra.Client client) {
        this.client = client;
        this.isValid = true;
    }

    @Override
    public boolean isValid() {
        return this.isValid;
    }

    @Override
    public Map<ByteBuffer, List<ColumnOrSuperColumn>> multiget_slice(
            String kvsMethodName,
            TableReference tableRef,
            List<ByteBuffer> keys,
            SlicePredicate predicate,
            ConsistencyLevel consistency_level)
            throws InvalidRequestException, UnavailableException, TimedOutException, TException {
        ColumnParent colFam = getColumnParent(tableRef);

        return executeMethod(() -> client.multiget_slice(keys, colFam, predicate, consistency_level));
    }

    @Override
    public List<KeySlice> get_range_slices(String kvsMethodName,
            TableReference tableRef,
            SlicePredicate predicate,
            KeyRange range,
            ConsistencyLevel consistency_level)
            throws InvalidRequestException, UnavailableException, TimedOutException, TException {
        ColumnParent colFam = getColumnParent(tableRef);

        return executeMethod(() -> client.get_range_slices(colFam, predicate, range, consistency_level));
    }

    @Override
    public String system_add_keyspace(KsDef ks_def)
            throws InvalidRequestException, SchemaDisagreementException, TException {
        return executeMethod(() -> client.system_add_keyspace(ks_def));
    }

    @Override
    public List<KsDef> describe_keyspaces() throws InvalidRequestException, TException {
        return executeMethod(() -> client.describe_keyspaces());
    }

    @Override
    public void batch_mutate(String kvsMethodName,
            Map<ByteBuffer, Map<String, List<Mutation>>> mutation_map,
            ConsistencyLevel consistency_level)
            throws InvalidRequestException, UnavailableException, TimedOutException, TException {
        executeMethod(() -> client.batch_mutate(mutation_map, consistency_level));
    }

    @Override
    public ColumnOrSuperColumn get(TableReference tableReference,
            ByteBuffer key,
            byte[] column,
            ConsistencyLevel consistency_level)
            throws InvalidRequestException, NotFoundException, UnavailableException, TimedOutException, TException {
        ColumnPath columnPath = new ColumnPath(tableReference.getQualifiedName());
        columnPath.setColumn(column);

        return executeMethod(() -> client.get(key, columnPath, consistency_level));
    }

    @Override
    public TProtocol getOutputProtocol() {
        return executeMethod(() -> client.getOutputProtocol());
    }

    @Override
    public TProtocol getInputProtocol() {
        return executeMethod(() -> client.getInputProtocol());
    }

    @Override
    public KsDef describe_keyspace(String keyspace) throws NotFoundException, InvalidRequestException, TException {
        return executeMethod(() -> client.describe_keyspace(keyspace));
    }

    @Override
    public String system_drop_column_family(String column_family)
            throws InvalidRequestException, SchemaDisagreementException, TException {
        return executeMethod(() -> client.system_drop_column_family(column_family));
    }

    @Override
    public void truncate(String cfname)
            throws InvalidRequestException, UnavailableException, TimedOutException, TException {
        executeMethod(() -> client.truncate(cfname));
    }

    @Override
    public String system_add_column_family(CfDef cf_def)
            throws InvalidRequestException, SchemaDisagreementException, TException {
        return executeMethod(() -> client.system_add_column_family(cf_def));
    }

    @Override
    public String system_update_column_family(CfDef cf_def)
            throws InvalidRequestException, SchemaDisagreementException, TException {
        return executeMethod(() -> client.system_update_column_family(cf_def));
    }

    @Override
    public String describe_partitioner() throws TException {
        return executeMethod(() -> client.describe_partitioner());
    }

    @Override
    public List<TokenRange> describe_ring(String keyspace) throws InvalidRequestException, TException {
        return executeMethod(() -> client.describe_ring(keyspace));
    }

    @Override
    public String describe_version() throws TException {
        return executeMethod(() -> client.describe_version());
    }

    @Override
    public String system_update_keyspace(KsDef ks_def)
            throws InvalidRequestException, SchemaDisagreementException, TException {
        return executeMethod(() -> client.system_update_keyspace(ks_def));
    }

    @Override
    public CqlPreparedResult prepare_cql3_query(ByteBuffer query, Compression compression)
            throws InvalidRequestException, TException {
        return executeMethod(() -> client.prepare_cql3_query(query, compression));
    }

    @Override
    public CqlResult execute_prepared_cql3_query(int intemId, List<ByteBuffer> values, ConsistencyLevel consistency)
            throws InvalidRequestException, UnavailableException, TimedOutException, SchemaDisagreementException, TException {
        return executeMethod(() -> client.execute_prepared_cql3_query(intemId, values, consistency));
    }

    @Override
    public ByteBuffer trace_next_query() throws TException {
        return executeMethod(() -> client.trace_next_query());
    }

    @Override
    public Map<String, List<String>> describe_schema_versions() throws InvalidRequestException, TException {
        return executeMethod(() -> client.describe_schema_versions());
    }

    @Override
    public CASResult cas(TableReference tableReference,
            ByteBuffer key,
            List<Column> expected,
            List<Column> updates,
            ConsistencyLevel serial_consistency_level,
            ConsistencyLevel commit_consistency_level)
            throws InvalidRequestException, UnavailableException, TimedOutException, TException {
        String internalTableName = AbstractKeyValueService.internalTableName(tableReference);

        return executeMethod(() -> client.cas(key, internalTableName, expected, updates, serial_consistency_level,
                commit_consistency_level));
    }

    @Override
    public CqlResult execute_cql3_query(CqlQuery cqlQuery,
            Compression compression,
            ConsistencyLevel consistency)
            throws InvalidRequestException, UnavailableException, TimedOutException, SchemaDisagreementException,
            TException {
        ByteBuffer queryBytes = ByteBuffer.wrap(cqlQuery.toString().getBytes(StandardCharsets.UTF_8));

        return executeMethod(() -> client.execute_cql3_query(queryBytes, compression, consistency));
    }

    private ColumnParent getColumnParent(TableReference tableRef) {
        return new ColumnParent(AbstractKeyValueService.internalTableName(tableRef));
    }

    private <T, E extends Throwable> T executeMethod(ThrowingSupplier<T, E> supplier)
            throws E {
        try {
            return supplier.apply();
        } catch (Exception e) {
            isValid = blackListedExceptions.stream()
                    .anyMatch(b -> e.getClass().isInstance(b));
            throw e;
        }
    }

    private <E extends Throwable> void executeMethod(ThrowingVoidSupplier<E> supplier)
            throws E {
        try {
            supplier.apply();
        } catch (Exception e) {
            isValid = blackListedExceptions.stream()
                    .anyMatch(b -> e.getClass().isInstance(b));
            throw Throwables.rewrapAndThrowUncheckedException(e);
        }
    }

    private interface ThrowingVoidSupplier<E extends Throwable> {
        void apply() throws E;
    }

    private interface ThrowingSupplier<T, E extends Throwable> {
        T apply() throws E;
    }
}
