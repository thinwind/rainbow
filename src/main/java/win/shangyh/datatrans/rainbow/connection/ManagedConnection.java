/* 
 * Copyright 2024 Shang Yehua <niceshang@outlook.com>
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
package win.shangyh.datatrans.rainbow.connection;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import java.sql.*;

import win.shangyh.datatrans.rainbow.DatabaseInfo;

/**
 *
 * 受管理的连接
 *
 * @author Shang Yehua <niceshang@outlook.com>
 * @since 2024-03-01  14:35
 *
 */
public class ManagedConnection implements Connection {
    
    private final static AtomicInteger ID_GENERATOR = new AtomicInteger(1);

    private final Connection innerConnection;

    private final ConnectionPoolManager poolManager;
    
    private final DatabaseInfo databaseInfo;
    
    private PreparedStatement preparedStatement;
    
    private boolean borrowed = false;
    
    private final int id;

    public ManagedConnection(Connection innerConnection, ConnectionPoolManager poolManager, DatabaseInfo databaseInfo) {
        this.innerConnection = innerConnection;
        this.poolManager = poolManager;
        id = ID_GENERATOR.getAndIncrement();
        this.databaseInfo = databaseInfo;
    }

    @Override
    public void close() {
        poolManager.returnConnection(this);
    }

    public void realClose() throws SQLException {
        innerConnection.close();
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return innerConnection.isWrapperFor(iface);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return innerConnection.unwrap(iface);
    }

    @Override
    public void clearWarnings() throws SQLException {
        innerConnection.clearWarnings();
    }

    @Override
    public void commit() throws SQLException {
        innerConnection.commit();
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return innerConnection.createArrayOf(typeName, elements);
    }

    @Override
    public Blob createBlob() throws SQLException {
        return innerConnection.createBlob();
    }

    @Override
    public Clob createClob() throws SQLException {
        return innerConnection.createClob();
    }

    @Override
    public NClob createNClob() throws SQLException {
        return innerConnection.createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        return innerConnection.createSQLXML();
    }

    @Override
    public Statement createStatement() throws SQLException {
        return innerConnection.createStatement();
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return innerConnection.createStatement(resultSetType, resultSetConcurrency);
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        return innerConnection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return innerConnection.createStruct(typeName, attributes);
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return innerConnection.getAutoCommit();
    }

    @Override
    public String getCatalog() throws SQLException {
        return innerConnection.getCatalog();
    }

    @Override
    public int getHoldability() throws SQLException {
        return innerConnection.getHoldability();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return innerConnection.getMetaData();
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return innerConnection.getTransactionIsolation();
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return innerConnection.getTypeMap();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return innerConnection.getWarnings();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return innerConnection.isClosed();
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return innerConnection.isReadOnly();
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        return innerConnection.nativeSQL(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        return innerConnection.prepareCall(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return innerConnection.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
        return innerConnection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        if(preparedStatement==null){
            synchronized (this) {
                if(preparedStatement==null){
                    preparedStatement = innerConnection.prepareStatement(sql);
                    return preparedStatement;
                }
            }
        }
        preparedStatement.addBatch(sql);
        return preparedStatement;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return innerConnection.prepareStatement(sql, autoGeneratedKeys);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return innerConnection.prepareStatement(sql, columnIndexes);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return innerConnection.prepareStatement(sql, columnNames);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
            throws SQLException {
        return innerConnection.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
        return innerConnection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        innerConnection.releaseSavepoint(savepoint);
    }

    @Override
    public void rollback() throws SQLException {
        innerConnection.rollback();
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        innerConnection.rollback(savepoint);
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        innerConnection.setAutoCommit(autoCommit);
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        innerConnection.setCatalog(catalog);
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        innerConnection.setHoldability(holdability);
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        innerConnection.setReadOnly(readOnly);
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        return innerConnection.setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        return innerConnection.setSavepoint(name);
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        innerConnection.setTransactionIsolation(level);
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        innerConnection.setTypeMap(map);
    }

    @Override
    public String getSchema() throws SQLException {
        return innerConnection.getSchema();
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        innerConnection.setSchema(schema);
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        innerConnection.abort(executor);
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        innerConnection.setNetworkTimeout(executor, milliseconds);
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return innerConnection.getNetworkTimeout();
    }

    @Override
    public void beginRequest() throws SQLException {
        innerConnection.beginRequest();
    }

    @Override
    public void endRequest() throws SQLException {
        innerConnection.endRequest();
    }

    @Override
    public boolean setShardingKeyIfValid(ShardingKey shardingKey, int timeout) throws SQLException {
        return innerConnection.setShardingKeyIfValid(shardingKey, timeout);
    }
    
    @Override
    public boolean setShardingKeyIfValid(ShardingKey shardingKey, ShardingKey superShardingKey, int timeout) throws SQLException {
        return innerConnection.setShardingKeyIfValid(shardingKey, superShardingKey, timeout);
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        return innerConnection.isValid(timeout);
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        innerConnection.setClientInfo(name, value);
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        innerConnection.setClientInfo(properties);
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        return innerConnection.getClientInfo(name);
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return innerConnection.getClientInfo();
    }
    
    public DatabaseInfo getDatabaseInfo() {
        return databaseInfo;
    }
    
    public boolean isBorrowed() {
        return borrowed;
    }
    
    public void setBorrowed(boolean borrowed) {
        this.borrowed = borrowed;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ManagedConnection other = (ManagedConnection) obj;
        return id == other.id;
    }
    
}
