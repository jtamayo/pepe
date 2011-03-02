package edu.stanford.pepe.jdt;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

public class PepeConnection implements Connection {

    private static final AtomicLong transactionCounter = new AtomicLong();
    
    private final Connection delegate;

    private final List<Object> statements = new ArrayList<Object>();
    
    public PepeConnection(Connection delegate) {
        System.out.println("Creating PepeConnection");
        this.delegate = delegate;
    }

    // PEPE Methods:
    protected void addStatement(Object statement) {
        this.statements.add(statement);
    }

    protected Fingerprint[][] computeFingerprints() throws SQLException {
        System.out.println("Computing fingerprints");
        if (getAutoCommit()) {
            throw new IllegalStateException("Cannot replay an auto-commit transaction");
        }
        
        // First rollback all changes (they'll be replayed anyway)
        // TODO: Should we change this for a savepoint instead?
        delegate.rollback();
        
        final List<SQLRead> reads = new ArrayList<SQLRead>();
        final List<SQLWrite> writes = new ArrayList<SQLWrite>();
        for (Object o : statements) {
            if (o instanceof SQLRead) {
                reads.add((SQLRead) o);
            } else if (o instanceof SQLWrite) {
                writes.add((SQLWrite) o);
            } else {
                throw new IllegalStateException("An object of type " + o.getClass()
                        + " cannot be in the list of statements.");
            }
        }
        // Slot is the space between two consecutive SQLWrites. For n SQLWrites there's n+1 slots. 
        final int slots = writes.size() + 1;
        
        // First dimension is the slot, second dim is the SQLRead. 
        Fingerprint[][] fingerprints = new Fingerprint[slots][reads.size()];
        
        // Execute slot 0, before any write
        for (int i = 0; i < reads.size(); i++) {
            fingerprints[0][i] = reads.get(i).replay(this);
        }
        
        // Execute the remaining writes and slots
        for (int slot = 1; slot < slots; slot++) {
            writes.get(slot - 1).execute(this);
            for (int i = 0; i < reads.size(); i++) {
                fingerprints[slot][i] = reads.get(i).replay(this);
            }
        }
        return fingerprints;
    }

    // JDBC Methods:

    private void log(Fingerprint[][] fingerprints) {
        List<StackTraceElement[]> queries = new ArrayList<StackTraceElement[]>();
        ArrayList<Boolean> isWrite = new ArrayList<Boolean>();
        for (Object o : statements) {
            if (o instanceof SQLRead) {
                queries.add(((SQLRead) o).getTrace());
                isWrite.add(false);
            } else if (o instanceof SQLWrite) {
                queries.add(((SQLWrite) o).getTrace());
                isWrite.add(true);
            } else {
                throw new IllegalStateException("An object of type " + o.getClass()
                        + " cannot be in the list of statements.");
            }
        }
        ResultLogger.log(transactionCounter.incrementAndGet(), queries, isWrite, fingerprints);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return delegate.unwrap(iface);
    }

    @Override
    public PepePreparedStatement prepareStatement(String sql) throws SQLException {
        return new PepePreparedStatement(delegate.prepareStatement(sql), sql, this);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        return new PepeCallableStatement(delegate.prepareCall(sql));
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        return delegate.nativeSQL(sql);
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        // If this method is called druing a tx, the tx is commited.
        if (getAutoCommit() == false) {
            // Tx was not autocommit, so it can be replayed
            logCommit();
        }
        delegate.setAutoCommit(autoCommit);
    }

    @Override
    public void commit() throws SQLException {
        logCommit();
        delegate.commit();
    }

    private void logCommit() throws SQLException {
        System.out.println("Commiting connection");
        Fingerprint[][] fingerprints = computeFingerprints();
        log(fingerprints);
        statements.clear();
    }
    
    @Override
    public void rollback() throws SQLException {
        System.out.println("Rolling-back connection");
        statements.clear();
        delegate.rollback();
    }

    @Override
    public void close() throws SQLException {
        System.out.println("Closing connection");
        delegate.close();
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        delegate.setReadOnly(readOnly);
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        delegate.setCatalog(catalog);
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        delegate.setTransactionIsolation(level);
    }

    @Override
    public void clearWarnings() throws SQLException {
        delegate.clearWarnings();
    }

    @Override
    public Array createArrayOf(String arg0, Object[] arg1) throws SQLException {
        return delegate.createArrayOf(arg0, arg1);
    }

    @Override
    public Blob createBlob() throws SQLException {
        return delegate.createBlob();
    }

    @Override
    public Clob createClob() throws SQLException {
        return delegate.createClob();
    }

    @Override
    public NClob createNClob() throws SQLException {
        return delegate.createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        return delegate.createSQLXML();
    }

    @Override
    public PepeStatement createStatement() throws SQLException {
        return new PepeStatement(delegate.createStatement());
    }

    @Override
    public PepeStatement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return new PepeStatement(delegate.createStatement(resultSetType, resultSetConcurrency));
    }

    @Override
    public PepePreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
            throws SQLException {
        return new PepePreparedStatement(delegate.prepareStatement(sql, resultSetType, resultSetConcurrency), sql, this);
    }

    @Override
    public PepeCallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
            throws SQLException {
        return new PepeCallableStatement(delegate.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetType));
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        delegate.setTypeMap(map);
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        // TODO: What to do with savepoints? Maybe it's best to just rule them all out
        return delegate.setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        return delegate.setSavepoint(name);
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        delegate.rollback(savepoint);
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        delegate.releaseSavepoint(savepoint);
    }

    @Override
    public PepeStatement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        return new PepeStatement(delegate.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability));
    }

    @Override
    public Struct createStruct(String arg0, Object[] arg1) throws SQLException {
        return delegate.createStruct(arg0, arg1);
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return delegate.getAutoCommit();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return delegate.isClosed();
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return delegate.isReadOnly();
    }

    @Override
    public String getCatalog() throws SQLException {
        return delegate.getCatalog();
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return delegate.getClientInfo();
    }

    @Override
    public String getClientInfo(String arg0) throws SQLException {
        return delegate.getClientInfo(arg0);
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return new PepeDatabaseMetaData(delegate.getMetaData(), this);
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return delegate.getTransactionIsolation();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return delegate.getWarnings();
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return delegate.getTypeMap();
    }

    @Override
    public int getHoldability() throws SQLException {
        return delegate.getHoldability();
    }

    @Override
    public boolean isValid(int arg0) throws SQLException {
        return delegate.isValid(arg0);
    }

    @Override
    public PepePreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
        return new PepePreparedStatement(delegate.prepareStatement(sql, resultSetType, resultSetConcurrency,
                resultSetHoldability), sql, this);
    }

    @Override
    public PepeCallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
        return new PepeCallableStatement(delegate.prepareCall(sql, resultSetType, resultSetConcurrency,
                resultSetHoldability));
    }

    @Override
    public PepePreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return new PepePreparedStatement(delegate.prepareStatement(sql, autoGeneratedKeys), sql, this);
    }

    @Override
    public PepePreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return new PepePreparedStatement(delegate.prepareStatement(sql, columnIndexes), sql, this);
    }

    @Override
    public PepePreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return new PepePreparedStatement(delegate.prepareStatement(sql, columnNames), sql, this);
    }

    @Override
    public void setClientInfo(Properties arg0) throws SQLClientInfoException {
        delegate.setClientInfo(arg0);
    }

    @Override
    public void setClientInfo(String arg0, String arg1) throws SQLClientInfoException {
        delegate.setClientInfo(arg0, arg1);
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        delegate.setHoldability(holdability);
    }

}
