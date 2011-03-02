package edu.stanford.pepe.jdt;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

public class PepeStatement extends PepeAbstractStatement implements Statement {
    
    private final Statement delegate;

    public PepeStatement(Statement delegate) {
        this.delegate = delegate;
    }
    
    public PepeStatement() {
        this.delegate = null;
    }

    public PepeResultSet executeQuery(String sql) throws SQLException {
        return new PepeResultSet(delegate.executeQuery(sql), this);
    }

    public int executeUpdate(String sql) throws SQLException {
        return delegate.executeUpdate(sql);
    }

    public void close() throws SQLException {
        delegate.close();
    }

    public int getMaxFieldSize() throws SQLException {
        return delegate.getMaxFieldSize();
    }

    public int getMaxRows() throws SQLException {
        return delegate.getMaxRows();
    }

    public int getQueryTimeout() throws SQLException {
        return delegate.getQueryTimeout();
    }

    public void cancel() throws SQLException {
        delegate.cancel();
    }

    public SQLWarning getWarnings() throws SQLException {
        return delegate.getWarnings();
    }

    public void clearWarnings() throws SQLException {
        delegate.clearWarnings();
    }

    public boolean execute(String sql) throws SQLException {
        return delegate.execute(sql);
    }

    public PepeResultSet getResultSet() throws SQLException {
        return new PepeResultSet(delegate.getResultSet(), this);
    }

    public int getUpdateCount() throws SQLException {
        return delegate.getUpdateCount();
    }

    public boolean getMoreResults() throws SQLException {
        return delegate.getMoreResults();
    }

    public int getFetchDirection() throws SQLException {
        return delegate.getFetchDirection();
    }

    public int getFetchSize() throws SQLException {
        return delegate.getFetchSize();
    }

    public int getResultSetConcurrency() throws SQLException {
        return delegate.getResultSetConcurrency();
    }

    public int getResultSetType() throws SQLException {
        return delegate.getResultSetType();
    }

    public void addBatch(String sql) throws SQLException {
        delegate.addBatch(sql);
    }

    public void clearBatch() throws SQLException {
        delegate.clearBatch();
    }

    public int[] executeBatch() throws SQLException {
        return delegate.executeBatch();
    }

    public Connection getConnection() throws SQLException {
        return delegate.getConnection();
    }

    public boolean getMoreResults(int current) throws SQLException {
        return delegate.getMoreResults(current);
    }

    public PepeResultSet getGeneratedKeys() throws SQLException {
        return new PepeResultSet(delegate.getGeneratedKeys(), this);
    }

    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return delegate.executeUpdate(sql, autoGeneratedKeys);
    }

    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return delegate.executeUpdate(sql, columnIndexes);
    }

    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return delegate.executeUpdate(sql, columnNames);
    }

    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return delegate.execute(sql, autoGeneratedKeys);
    }

    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        return delegate.execute(sql, columnIndexes);
    }

    public boolean execute(String sql, String[] columnNames) throws SQLException {
        return delegate.execute(sql, columnNames);
    }

    public int getResultSetHoldability() throws SQLException {
        return delegate.getResultSetHoldability();
    }

    public boolean isClosed() throws SQLException {
        return delegate.isClosed();
    }

    public boolean isPoolable() throws SQLException {
        return delegate.isPoolable();
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }

    public void setMaxFieldSize(int max) throws SQLException {
        delegate.setMaxFieldSize(max);
    }

    public void setMaxRows(int max) throws SQLException {
        delegate.setMaxRows(max);
    }

    public void setEscapeProcessing(boolean enable) throws SQLException {
        delegate.setEscapeProcessing(enable);
    }

    public void setCursorName(String name) throws SQLException {
        delegate.setCursorName(name);
    }

    public void setFetchDirection(int direction) throws SQLException {
        delegate.setFetchDirection(direction);
    }

    public void setFetchSize(int rows) throws SQLException {
        delegate.setFetchSize(rows);
    }

    public void setPoolable(boolean poolable) throws SQLException {
        delegate.setPoolable(poolable);
    }

    public void setQueryTimeout(int seconds) throws SQLException {
        delegate.setQueryTimeout(seconds);
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return delegate.unwrap(iface);
    }

}
