package edu.stanford.pepe.jdt;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import edu.stanford.pepe.jdt.commands.ClearParameters;
import edu.stanford.pepe.jdt.commands.Command;
import edu.stanford.pepe.jdt.commands.SetBigDecimal;
import edu.stanford.pepe.jdt.commands.SetBoolean;
import edu.stanford.pepe.jdt.commands.SetByte;
import edu.stanford.pepe.jdt.commands.SetDate;
import edu.stanford.pepe.jdt.commands.SetDouble;
import edu.stanford.pepe.jdt.commands.SetEscapeProcessing;
import edu.stanford.pepe.jdt.commands.SetFloat;
import edu.stanford.pepe.jdt.commands.SetInt;
import edu.stanford.pepe.jdt.commands.SetLong;
import edu.stanford.pepe.jdt.commands.SetMaxFieldSize;
import edu.stanford.pepe.jdt.commands.SetMaxRows;
import edu.stanford.pepe.jdt.commands.SetNull;
import edu.stanford.pepe.jdt.commands.SetObject;
import edu.stanford.pepe.jdt.commands.SetShort;
import edu.stanford.pepe.jdt.commands.SetString;
import edu.stanford.pepe.jdt.commands.SetTime;
import edu.stanford.pepe.jdt.commands.SetTimestamp;
import edu.stanford.pepe.jdt.commands.SetURL;
import edu.stanford.pepe.jdt.commands.UnsupportedCommand;

public class PepePreparedStatement extends PepeAbstractStatement implements PreparedStatement {

    private final List<Command> history = new ArrayList<Command>();

    private final PreparedStatement delegate;

    private final PepeConnection connection;

    private final String sql;

    public PepePreparedStatement(PreparedStatement delegate, String sql, PepeConnection connection) {
        this.delegate = delegate;
        this.connection = connection;
        this.sql = sql;
    }

    public PepeResultSet executeQuery() throws SQLException {
        final ResultSet rs = delegate.executeQuery();
        connection.addStatement(new SQLRead(sql, history));
        return new PepeResultSet(rs, this);
    }

    public int executeUpdate() throws SQLException {
        final int executeUpdate = delegate.executeUpdate();
        connection.addStatement(new SQLWrite(sql, history, SQLWrite.Type.EXECUTE_UPDATE));
        return executeUpdate;
    }

    public boolean execute() throws SQLException {
        final boolean execute = delegate.execute();
        connection.addStatement(new SQLWrite(sql, history, SQLWrite.Type.EXECUTE));
        return execute;
    }

    public void addBatch(String sql) throws SQLException {
        throw new UnsupportedOperationException();
        //        delegate.addBatch(sql);
    }

    public void clearBatch() throws SQLException {
        throw new UnsupportedOperationException();
        //        delegate.clearBatch();
    }

    public void addBatch() throws SQLException {
        throw new UnsupportedOperationException();
        //        delegate.addBatch();
    }

    public int[] executeBatch() throws SQLException {
        throw new UnsupportedOperationException();
        //        final int[] executeBatch = delegate.executeBatch();
        //        connection.addStatement(new SQLWrite(sql, new ArrayList<Command>(history), SQLWrite.Type.EXECUTE_BATCH));
        //        return executeBatch;
    }

    public boolean getMoreResults(int current) throws SQLException {
        return delegate.getMoreResults(current);
    }

    public int getUpdateCount() throws SQLException {
        return delegate.getUpdateCount();
    }

    public boolean getMoreResults() throws SQLException {
        // Question: What should I do about statements that return multiple ResultSets?
        // Answer: This can only happen when I execute() a statement that returns multiple
        // ResultSets/update counts. Since execute() is logged as a write, it doesn't 
        // matter, because it is not moved in the replay.
        return delegate.getMoreResults();
    }

    // Other methods
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return delegate.getParameterMetaData();
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

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return delegate.unwrap(iface);
    }

    public PepeResultSet getResultSet() throws SQLException {
        return new PepeResultSet(delegate.getResultSet(), this);
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        return new PepeResultSet(delegate.getResultSet(), this);
    }

    // Prohibited overrides from Statement
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        throw new SQLException("Not allowed for a PreparedStatement");
    }

    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        throw new SQLException("Not allowed for a PreparedStatement");
    }

    public int executeUpdate(String sql) throws SQLException {
        throw new SQLException("Not allowed for a PreparedStatement");
    }

    public boolean execute(String sql) throws SQLException {
        throw new SQLException("Not allowed for a PreparedStatement");
    }

    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        throw new SQLException("Not allowed for a PreparedStatement");
    }

    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        throw new SQLException("Not allowed for a PreparedStatement");
    }

    public boolean execute(String sql, String[] columnNames) throws SQLException {
        throw new SQLException("Not allowed for a PreparedStatement");
    }

    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        throw new SQLException("Not allowed for a PreparedStatement");
    }

    public PepeResultSet executeQuery(String sql) throws SQLException {
        throw new SQLException("Not allowed for a PreparedStatement");
    }

    // Loggable methods that change the state of the PreparedStatement
    public void clearParameters() throws SQLException {
        delegate.clearParameters();
        history.add(new ClearParameters());
    }

    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        delegate.setAsciiStream(parameterIndex, x, length);
        history.add(new UnsupportedCommand("setAsciiStream"));
    }

    public void setArray(int i, Array x) throws SQLException {
        delegate.setArray(i, x);
        history.add(new UnsupportedCommand("setArray"));
    }

    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        delegate.setAsciiStream(parameterIndex, x, length);
        history.add(new UnsupportedCommand("setAsciiStream"));
    }

    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        delegate.setAsciiStream(parameterIndex, x);
        history.add(new UnsupportedCommand("setAsciiStream"));
    }

    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        delegate.setBigDecimal(parameterIndex, x);
        history.add(new SetBigDecimal(parameterIndex, x));
    }

    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        delegate.setBinaryStream(parameterIndex, x, length);
        history.add(new UnsupportedCommand("setBinaryStream"));
    }

    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        delegate.setBinaryStream(parameterIndex, x, length);
        history.add(new UnsupportedCommand("setBinaryStream"));
    }

    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        delegate.setBinaryStream(parameterIndex, x);
        history.add(new UnsupportedCommand("setBinaryStream"));
    }

    public void setBlob(int i, Blob x) throws SQLException {
        delegate.setBlob(i, x);
        history.add(new UnsupportedCommand("setBlob"));
    }

    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        delegate.setBlob(parameterIndex, inputStream, length);
        history.add(new UnsupportedCommand("setBlob"));
    }

    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        delegate.setBlob(parameterIndex, inputStream);
        history.add(new UnsupportedCommand("setBlob"));
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        delegate.setBoolean(parameterIndex, x);
        history.add(new SetBoolean(parameterIndex, x));
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
        delegate.setByte(parameterIndex, x);
        history.add(new SetByte(parameterIndex, x));
    }

    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        delegate.setBytes(parameterIndex, x);
        history.add(new UnsupportedCommand("setBytes"));
    }

    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        delegate.setCharacterStream(parameterIndex, reader, length);
        history.add(new UnsupportedCommand("setCharacterStream"));
    }

    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        delegate.setCharacterStream(parameterIndex, reader, length);
        history.add(new UnsupportedCommand("setCharacterStream"));
    }

    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        delegate.setCharacterStream(parameterIndex, reader);
        history.add(new UnsupportedCommand("setCharacterStream"));
    }

    public void setClob(int i, Clob x) throws SQLException {
        delegate.setClob(i, x);
        history.add(new UnsupportedCommand("setClob"));
    }

    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        delegate.setClob(parameterIndex, reader, length);
        history.add(new UnsupportedCommand("setClob"));
    }

    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        delegate.setClob(parameterIndex, reader);
        history.add(new UnsupportedCommand("setClob"));
    }

    public void setMaxFieldSize(int max) throws SQLException {
        delegate.setMaxFieldSize(max);
        history.add(new SetMaxFieldSize(max));
    }

    public void setInt(int parameterIndex, int x) throws SQLException {
        delegate.setInt(parameterIndex, x);
        history.add(new SetInt(parameterIndex, x));
    }

    public void setLong(int parameterIndex, long x) throws SQLException {
        delegate.setLong(parameterIndex, x);
        history.add(new SetLong(parameterIndex, x));
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
        delegate.setFloat(parameterIndex, x);
        history.add(new SetFloat(parameterIndex, x));
    }

    public void setMaxRows(int max) throws SQLException {
        delegate.setMaxRows(max);
        history.add(new SetMaxRows(max));
    }

    public void setDouble(int parameterIndex, double x) throws SQLException {
        delegate.setDouble(parameterIndex, x);
        history.add(new SetDouble(parameterIndex, x));
    }

    public void setEscapeProcessing(boolean enable) throws SQLException {
        delegate.setEscapeProcessing(enable);
        history.add(new SetEscapeProcessing(enable));
    }

    public void setDate(int parameterIndex, Date x) throws SQLException {
        delegate.setDate(parameterIndex, x);
        history.add(new SetDate(parameterIndex, x));
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

    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        delegate.setDate(parameterIndex, x, cal);
        history.add(new SetDate(parameterIndex, x, cal));
    }

    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        delegate.setNCharacterStream(parameterIndex, value, length);
        history.add(new UnsupportedCommand("setNCharacterStream"));
    }

    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        delegate.setNCharacterStream(parameterIndex, value);
        history.add(new UnsupportedCommand("setNCharacterStream"));
    }

    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        delegate.setNClob(parameterIndex, value);
        history.add(new UnsupportedCommand("setNClob"));
    }

    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        delegate.setNClob(parameterIndex, reader, length);
        history.add(new UnsupportedCommand("setNClob"));
    }

    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        delegate.setNClob(parameterIndex, reader);
        history.add(new UnsupportedCommand("setNClob"));
    }

    public void setNString(int parameterIndex, String value) throws SQLException {
        delegate.setNString(parameterIndex, value);
        history.add(new UnsupportedCommand("setNString"));
    }

    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        delegate.setNull(parameterIndex, sqlType);
        history.add(new SetNull(parameterIndex, sqlType));
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException {
        delegate.setObject(parameterIndex, x, targetSqlType, scale);
        history.add(new SetObject(parameterIndex, x, targetSqlType, scale));
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        delegate.setObject(parameterIndex, x, targetSqlType);
        history.add(new SetObject(parameterIndex, x, targetSqlType));
    }

    public void setObject(int parameterIndex, Object x) throws SQLException {
        delegate.setObject(parameterIndex, x);
        history.add(new SetObject(parameterIndex, x));
    }

    public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException {
        delegate.setNull(paramIndex, sqlType, typeName);
        history.add(new SetNull(paramIndex, sqlType, typeName));
    }

    public void setPoolable(boolean poolable) throws SQLException {
        delegate.setPoolable(poolable);
    }

    public void setQueryTimeout(int seconds) throws SQLException {
        delegate.setQueryTimeout(seconds);
    }

    public void setRef(int i, Ref x) throws SQLException {
        delegate.setRef(i, x);
        history.add(new UnsupportedCommand("setRef"));
    }

    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        delegate.setRowId(parameterIndex, x);
    }

    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        delegate.setSQLXML(parameterIndex, xmlObject);
        history.add(new UnsupportedCommand("setSQLXML"));
    }

    public void setShort(int parameterIndex, short x) throws SQLException {
        delegate.setShort(parameterIndex, x);
        history.add(new SetShort(parameterIndex, x));
    }

    public void setString(int parameterIndex, String x) throws SQLException {
        delegate.setString(parameterIndex, x);
        history.add(new SetString(parameterIndex, x));
    }

    public void setTime(int parameterIndex, Time x) throws SQLException {
        delegate.setTime(parameterIndex, x);
        history.add(new SetTime(parameterIndex, x));
    }

    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        delegate.setTimestamp(parameterIndex, x);
        history.add(new SetTimestamp(parameterIndex, x));
    }

    @SuppressWarnings("deprecation")
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        delegate.setUnicodeStream(parameterIndex, x, length);
        history.add(new UnsupportedCommand("setRef"));
    }

    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        delegate.setTime(parameterIndex, x, cal);
        history.add(new SetTime(parameterIndex, x, cal));
    }

    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        delegate.setTimestamp(parameterIndex, x, cal);
        history.add(new SetTimestamp(parameterIndex, x, cal));
    }

    public void setURL(int parameterIndex, URL x) throws SQLException {
        delegate.setURL(parameterIndex, x);
        history.add(new SetURL(parameterIndex, x));
    }

    // Other methods

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
        // TODO: Should I do something about canceling statements?
        delegate.cancel();
    }

    public SQLWarning getWarnings() throws SQLException {
        return delegate.getWarnings();
    }

    public void clearWarnings() throws SQLException {
        delegate.clearWarnings();
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

    public PepeConnection getConnection() throws SQLException {
        return this.connection;
    }

    public PepeResultSetMetaData getMetaData() throws SQLException {
        return new PepeResultSetMetaData(delegate.getMetaData());
    }

    /**
     * Executes the wrapped PreparedStatement without logging its execution to
     * the PepeConnection.
     * 
     * @throws SQLException
     *             exceptions thrown by the underlying PreparedStatement.
     */
    public int executeUpdateNoLog() throws SQLException {
        return delegate.executeUpdate();
    }

    /**
     * Executes the wrapped PreparedStatement without logging its execution to
     * the PepeConnection.
     * 
     * @throws SQLException
     *             exceptions thrown by the underlying PreparedStatement.
     */
    public int[] executeBatchNoLog() throws SQLException {
        return delegate.executeBatch();
    }

    /**
     * Executes the wrapped PreparedStatement without logging its execution to
     * the PepeConnection.
     * 
     * @throws SQLException
     *             exceptions thrown by the underlying PreparedStatement.
     */
    public boolean executeNoLog() throws SQLException {
        return delegate.execute();
    }

    /**
     * Executes the wrapped PreparedStatement without logging its execution to
     * the PepeConnection.
     * 
     * @throws SQLException
     *             exceptions thrown by the underlying PreparedStatement.
     */
    public PepeResultSet executeQueryNoLog() throws SQLException {
        ResultSet rs = delegate.executeQuery();
        return new PepeResultSet(rs, this);
    }

}
