package edu.stanford.pepe.jdt;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

public class PepeCallableStatement implements CallableStatement {

    private final CallableStatement delegate;

    public PepeCallableStatement(CallableStatement delegate) {
        this.delegate = delegate;
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        return delegate.executeQuery(sql);
    }

    public ResultSet executeQuery() throws SQLException {
        return delegate.executeQuery();
    }

    public int executeUpdate(String sql) throws SQLException {
        return delegate.executeUpdate(sql);
    }

    public int executeUpdate() throws SQLException {
        return delegate.executeUpdate();
    }

    public void close() throws SQLException {
        delegate.close();
    }

    public int getMaxFieldSize() throws SQLException {
        return delegate.getMaxFieldSize();
    }

    public void setMaxFieldSize(int max) throws SQLException {
        delegate.setMaxFieldSize(max);
    }

    public void setInt(int parameterIndex, int x) throws SQLException {
        delegate.setInt(parameterIndex, x);
    }

    public int getMaxRows() throws SQLException {
        return delegate.getMaxRows();
    }

    public void setLong(int parameterIndex, long x) throws SQLException {
        delegate.setLong(parameterIndex, x);
    }

    public void setMaxRows(int max) throws SQLException {
        delegate.setMaxRows(max);
    }

    public boolean getBoolean(int parameterIndex) throws SQLException {
        return delegate.getBoolean(parameterIndex);
    }

    public byte getByte(int parameterIndex) throws SQLException {
        return delegate.getByte(parameterIndex);
    }

    public int getQueryTimeout() throws SQLException {
        return delegate.getQueryTimeout();
    }

    public int getInt(int parameterIndex) throws SQLException {
        return delegate.getInt(parameterIndex);
    }

    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        delegate.setBytes(parameterIndex, x);
    }

    public void cancel() throws SQLException {
        delegate.cancel();
    }

    public long getLong(int parameterIndex) throws SQLException {
        return delegate.getLong(parameterIndex);
    }

    public float getFloat(int parameterIndex) throws SQLException {
        return delegate.getFloat(parameterIndex);
    }

    public void setTime(int parameterIndex, Time x) throws SQLException {
        delegate.setTime(parameterIndex, x);
    }

    public double getDouble(int parameterIndex) throws SQLException {
        return delegate.getDouble(parameterIndex);
    }

    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        delegate.setTimestamp(parameterIndex, x);
    }

    public void clearWarnings() throws SQLException {
        delegate.clearWarnings();
    }

    public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException {
        return delegate.getBigDecimal(parameterIndex, scale);
    }

    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        delegate.setAsciiStream(parameterIndex, x, length);
    }

    public byte[] getBytes(int parameterIndex) throws SQLException {
        return delegate.getBytes(parameterIndex);
    }

    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        delegate.setUnicodeStream(parameterIndex, x, length);
    }

    public Date getDate(int parameterIndex) throws SQLException {
        return delegate.getDate(parameterIndex);
    }

    public boolean execute(String sql) throws SQLException {
        return delegate.execute(sql);
    }

    public ResultSet getResultSet() throws SQLException {
        return delegate.getResultSet();
    }

    public Object getObject(int parameterIndex) throws SQLException {
        return delegate.getObject(parameterIndex);
    }

    public void clearParameters() throws SQLException {
        delegate.clearParameters();
    }

    public boolean getMoreResults() throws SQLException {
        return delegate.getMoreResults();
    }

    public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
        return delegate.getBigDecimal(parameterIndex);
    }

    public int getFetchDirection() throws SQLException {
        return delegate.getFetchDirection();
    }

    public Ref getRef(int i) throws SQLException {
        return delegate.getRef(i);
    }

    public Blob getBlob(int i) throws SQLException {
        return delegate.getBlob(i);
    }

    public int getFetchSize() throws SQLException {
        return delegate.getFetchSize();
    }

    public Array getArray(int i) throws SQLException {
        return delegate.getArray(i);
    }

    public int getResultSetConcurrency() throws SQLException {
        return delegate.getResultSetConcurrency();
    }

    public int getResultSetType() throws SQLException {
        return delegate.getResultSetType();
    }

    public boolean execute() throws SQLException {
        return delegate.execute();
    }

    public void addBatch(String sql) throws SQLException {
        delegate.addBatch(sql);
    }

    public void clearBatch() throws SQLException {
        delegate.clearBatch();
    }

    public void addBatch() throws SQLException {
        delegate.addBatch();
    }

    public int[] executeBatch() throws SQLException {
        return delegate.executeBatch();
    }

    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        delegate.setCharacterStream(parameterIndex, reader, length);
    }

    public void setArray(int i, Array x) throws SQLException {
        delegate.setArray(i, x);
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        return delegate.getMetaData();
    }

    public void registerOutParameter(String parameterName, int sqlType) throws SQLException {
        delegate.registerOutParameter(parameterName, sqlType);
    }

    public boolean getMoreResults(int current) throws SQLException {
        return delegate.getMoreResults(current);
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        return delegate.getGeneratedKeys();
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

    public ParameterMetaData getParameterMetaData() throws SQLException {
        return delegate.getParameterMetaData();
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

    public boolean getBoolean(String parameterName) throws SQLException {
        return delegate.getBoolean(parameterName);
    }

    public byte getByte(String parameterName) throws SQLException {
        return delegate.getByte(parameterName);
    }

    public int getInt(String parameterName) throws SQLException {
        return delegate.getInt(parameterName);
    }

    public long getLong(String parameterName) throws SQLException {
        return delegate.getLong(parameterName);
    }

    public float getFloat(String parameterName) throws SQLException {
        return delegate.getFloat(parameterName);
    }

    public double getDouble(String parameterName) throws SQLException {
        return delegate.getDouble(parameterName);
    }

    public byte[] getBytes(String parameterName) throws SQLException {
        return delegate.getBytes(parameterName);
    }

    public Date getDate(String parameterName) throws SQLException {
        return delegate.getDate(parameterName);
    }

    public BigDecimal getBigDecimal(String parameterName) throws SQLException {
        return delegate.getBigDecimal(parameterName);
    }

    public Blob getBlob(String parameterName) throws SQLException {
        return delegate.getBlob(parameterName);
    }

    public Array getArray(String parameterName) throws SQLException {
        return delegate.getArray(parameterName);
    }

    public Reader getCharacterStream(int parameterIndex) throws SQLException {
        return delegate.getCharacterStream(parameterIndex);
    }

    public Reader getCharacterStream(String parameterName) throws SQLException {
        return delegate.getCharacterStream(parameterName);
    }

    public Clob getClob(int i) throws SQLException {
        return delegate.getClob(i);
    }

    public Date getDate(int parameterIndex, Calendar cal) throws SQLException {
        return delegate.getDate(parameterIndex, cal);
    }

    public Connection getConnection() throws SQLException {
        return delegate.getConnection();
    }

    public Clob getClob(String parameterName) throws SQLException {
        return delegate.getClob(parameterName);
    }

    public Date getDate(String parameterName, Calendar cal) throws SQLException {
        return delegate.getDate(parameterName, cal);
    }

    public Reader getNCharacterStream(int parameterIndex) throws SQLException {
        return delegate.getNCharacterStream(parameterIndex);
    }

    public Reader getNCharacterStream(String parameterName) throws SQLException {
        return delegate.getNCharacterStream(parameterName);
    }

    public NClob getNClob(int parameterIndex) throws SQLException {
        return delegate.getNClob(parameterIndex);
    }

    public NClob getNClob(String parameterName) throws SQLException {
        return delegate.getNClob(parameterName);
    }

    public String getNString(int parameterIndex) throws SQLException {
        return delegate.getNString(parameterIndex);
    }

    public String getNString(String parameterName) throws SQLException {
        return delegate.getNString(parameterName);
    }

    public Object getObject(int i, Map<String, Class<?>> map) throws SQLException {
        return delegate.getObject(i, map);
    }

    public Object getObject(String parameterName) throws SQLException {
        return delegate.getObject(parameterName);
    }

    public Object getObject(String parameterName, Map<String, Class<?>> map) throws SQLException {
        return delegate.getObject(parameterName, map);
    }

    public Ref getRef(String parameterName) throws SQLException {
        return delegate.getRef(parameterName);
    }

    public RowId getRowId(int parameterIndex) throws SQLException {
        return delegate.getRowId(parameterIndex);
    }

    public RowId getRowId(String parameterName) throws SQLException {
        return delegate.getRowId(parameterName);
    }

    public SQLXML getSQLXML(int parameterIndex) throws SQLException {
        return delegate.getSQLXML(parameterIndex);
    }

    public SQLXML getSQLXML(String parameterName) throws SQLException {
        return delegate.getSQLXML(parameterName);
    }

    public String getString(int parameterIndex) throws SQLException {
        return delegate.getString(parameterIndex);
    }

    public short getShort(int parameterIndex) throws SQLException {
        return delegate.getShort(parameterIndex);
    }

    public SQLWarning getWarnings() throws SQLException {
        return delegate.getWarnings();
    }

    public Time getTime(int parameterIndex) throws SQLException {
        return delegate.getTime(parameterIndex);
    }

    public Timestamp getTimestamp(int parameterIndex) throws SQLException {
        return delegate.getTimestamp(parameterIndex);
    }

    public int getUpdateCount() throws SQLException {
        return delegate.getUpdateCount();
    }

    public Time getTime(int parameterIndex, Calendar cal) throws SQLException {
        return delegate.getTime(parameterIndex, cal);
    }

    public Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException {
        return delegate.getTimestamp(parameterIndex, cal);
    }

    public URL getURL(int parameterIndex) throws SQLException {
        return delegate.getURL(parameterIndex);
    }

    public String getString(String parameterName) throws SQLException {
        return delegate.getString(parameterName);
    }

    public short getShort(String parameterName) throws SQLException {
        return delegate.getShort(parameterName);
    }

    public Time getTime(String parameterName) throws SQLException {
        return delegate.getTime(parameterName);
    }

    public Timestamp getTimestamp(String parameterName) throws SQLException {
        return delegate.getTimestamp(parameterName);
    }

    public Time getTime(String parameterName, Calendar cal) throws SQLException {
        return delegate.getTime(parameterName, cal);
    }

    public Timestamp getTimestamp(String parameterName, Calendar cal) throws SQLException {
        return delegate.getTimestamp(parameterName, cal);
    }

    public URL getURL(String parameterName) throws SQLException {
        return delegate.getURL(parameterName);
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

    public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException {
        delegate.registerOutParameter(parameterIndex, sqlType);
    }

    public void registerOutParameter(int parameterIndex, int sqlType, int scale) throws SQLException {
        delegate.registerOutParameter(parameterIndex, sqlType, scale);
    }

    public void registerOutParameter(int paramIndex, int sqlType, String typeName) throws SQLException {
        delegate.registerOutParameter(paramIndex, sqlType, typeName);
    }

    public void registerOutParameter(String parameterName, int sqlType, int scale) throws SQLException {
        delegate.registerOutParameter(parameterName, sqlType, scale);
    }

    public void registerOutParameter(String parameterName, int sqlType, String typeName) throws SQLException {
        delegate.registerOutParameter(parameterName, sqlType, typeName);
    }

    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        delegate.setAsciiStream(parameterIndex, x, length);
    }

    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        delegate.setAsciiStream(parameterIndex, x);
    }

    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        delegate.setTime(parameterIndex, x, cal);
    }

    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        delegate.setTimestamp(parameterIndex, x, cal);
    }

    public void setURL(String parameterName, URL val) throws SQLException {
        delegate.setURL(parameterName, val);
    }

    public void setURL(int parameterIndex, URL x) throws SQLException {
        delegate.setURL(parameterIndex, x);
    }

    public void setInt(String parameterName, int x) throws SQLException {
        delegate.setInt(parameterName, x);
    }

    public void setLong(String parameterName, long x) throws SQLException {
        delegate.setLong(parameterName, x);
    }

    public void setBytes(String parameterName, byte[] x) throws SQLException {
        delegate.setBytes(parameterName, x);
    }

    public void setAsciiStream(String parameterName, InputStream x, int length) throws SQLException {
        delegate.setAsciiStream(parameterName, x, length);
    }

    public void setAsciiStream(String parameterName, InputStream x, long length) throws SQLException {
        delegate.setAsciiStream(parameterName, x, length);
    }

    public void setAsciiStream(String parameterName, InputStream x) throws SQLException {
        delegate.setAsciiStream(parameterName, x);
    }

    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        delegate.setBigDecimal(parameterIndex, x);
    }

    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        delegate.setBinaryStream(parameterIndex, x, length);
    }

    public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException {
        delegate.setBigDecimal(parameterName, x);
    }

    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        delegate.setBinaryStream(parameterIndex, x, length);
    }

    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        delegate.setBinaryStream(parameterIndex, x);
    }

    public void setBinaryStream(String parameterName, InputStream x, int length) throws SQLException {
        delegate.setBinaryStream(parameterName, x, length);
    }

    public void setBinaryStream(String parameterName, InputStream x, long length) throws SQLException {
        delegate.setBinaryStream(parameterName, x, length);
    }

    public void setBinaryStream(String parameterName, InputStream x) throws SQLException {
        delegate.setBinaryStream(parameterName, x);
    }

    public void setBlob(int i, Blob x) throws SQLException {
        delegate.setBlob(i, x);
    }

    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        delegate.setBlob(parameterIndex, inputStream, length);
    }

    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        delegate.setBlob(parameterIndex, inputStream);
    }

    public void setBlob(String parameterName, Blob x) throws SQLException {
        delegate.setBlob(parameterName, x);
    }

    public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException {
        delegate.setBlob(parameterName, inputStream, length);
    }

    public void setBlob(String parameterName, InputStream inputStream) throws SQLException {
        delegate.setBlob(parameterName, inputStream);
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        delegate.setBoolean(parameterIndex, x);
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
        delegate.setByte(parameterIndex, x);
    }

    public void setBoolean(String parameterName, boolean x) throws SQLException {
        delegate.setBoolean(parameterName, x);
    }

    public void setByte(String parameterName, byte x) throws SQLException {
        delegate.setByte(parameterName, x);
    }

    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        delegate.setCharacterStream(parameterIndex, reader, length);
    }

    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        delegate.setCharacterStream(parameterIndex, reader);
    }

    public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException {
        delegate.setCharacterStream(parameterName, reader, length);
    }

    public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException {
        delegate.setCharacterStream(parameterName, reader, length);
    }

    public void setCharacterStream(String parameterName, Reader reader) throws SQLException {
        delegate.setCharacterStream(parameterName, reader);
    }

    public void setClob(int i, Clob x) throws SQLException {
        delegate.setClob(i, x);
    }

    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        delegate.setClob(parameterIndex, reader, length);
    }

    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        delegate.setClob(parameterIndex, reader);
    }

    public void setClob(String parameterName, Clob x) throws SQLException {
        delegate.setClob(parameterName, x);
    }

    public void setClob(String parameterName, Reader reader, long length) throws SQLException {
        delegate.setClob(parameterName, reader, length);
    }

    public void setClob(String parameterName, Reader reader) throws SQLException {
        delegate.setClob(parameterName, reader);
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
        delegate.setFloat(parameterIndex, x);
    }

    public void setDouble(int parameterIndex, double x) throws SQLException {
        delegate.setDouble(parameterIndex, x);
    }

    public void setEscapeProcessing(boolean enable) throws SQLException {
        delegate.setEscapeProcessing(enable);
    }

    public void setDate(int parameterIndex, Date x) throws SQLException {
        delegate.setDate(parameterIndex, x);
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
    }

    public void setFloat(String parameterName, float x) throws SQLException {
        delegate.setFloat(parameterName, x);
    }

    public void setDouble(String parameterName, double x) throws SQLException {
        delegate.setDouble(parameterName, x);
    }

    public void setDate(String parameterName, Date x) throws SQLException {
        delegate.setDate(parameterName, x);
    }

    public void setDate(String parameterName, Date x, Calendar cal) throws SQLException {
        delegate.setDate(parameterName, x, cal);
    }

    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        delegate.setNCharacterStream(parameterIndex, value, length);
    }

    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        delegate.setNCharacterStream(parameterIndex, value);
    }

    public void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException {
        delegate.setNCharacterStream(parameterName, value, length);
    }

    public void setNCharacterStream(String parameterName, Reader value) throws SQLException {
        delegate.setNCharacterStream(parameterName, value);
    }

    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        delegate.setNClob(parameterIndex, value);
    }

    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        delegate.setNClob(parameterIndex, reader, length);
    }

    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        delegate.setNClob(parameterIndex, reader);
    }

    public void setNClob(String parameterName, NClob value) throws SQLException {
        delegate.setNClob(parameterName, value);
    }

    public void setNClob(String parameterName, Reader reader, long length) throws SQLException {
        delegate.setNClob(parameterName, reader, length);
    }

    public void setNClob(String parameterName, Reader reader) throws SQLException {
        delegate.setNClob(parameterName, reader);
    }

    public void setNString(int parameterIndex, String value) throws SQLException {
        delegate.setNString(parameterIndex, value);
    }

    public void setNString(String parameterName, String value) throws SQLException {
        delegate.setNString(parameterName, value);
    }

    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        delegate.setNull(parameterIndex, sqlType);
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException {
        delegate.setObject(parameterIndex, x, targetSqlType, scale);
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        delegate.setObject(parameterIndex, x, targetSqlType);
    }

    public void setObject(int parameterIndex, Object x) throws SQLException {
        delegate.setObject(parameterIndex, x);
    }

    public void setNull(int paramIndex, int sqlType, String typeName) throws SQLException {
        delegate.setNull(paramIndex, sqlType, typeName);
    }

    public void setNull(String parameterName, int sqlType) throws SQLException {
        delegate.setNull(parameterName, sqlType);
    }

    public void setTime(String parameterName, Time x) throws SQLException {
        delegate.setTime(parameterName, x);
    }

    public void setTimestamp(String parameterName, Timestamp x) throws SQLException {
        delegate.setTimestamp(parameterName, x);
    }

    public void setObject(String parameterName, Object x, int targetSqlType, int scale) throws SQLException {
        delegate.setObject(parameterName, x, targetSqlType, scale);
    }

    public void setObject(String parameterName, Object x, int targetSqlType) throws SQLException {
        delegate.setObject(parameterName, x, targetSqlType);
    }

    public void setObject(String parameterName, Object x) throws SQLException {
        delegate.setObject(parameterName, x);
    }

    public void setNull(String parameterName, int sqlType, String typeName) throws SQLException {
        delegate.setNull(parameterName, sqlType, typeName);
    }

    public void setPoolable(boolean poolable) throws SQLException {
        delegate.setPoolable(poolable);
    }

    public void setQueryTimeout(int seconds) throws SQLException {
        delegate.setQueryTimeout(seconds);
    }

    public void setRef(int i, Ref x) throws SQLException {
        delegate.setRef(i, x);
    }

    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        delegate.setRowId(parameterIndex, x);
    }

    public void setRowId(String parameterName, RowId x) throws SQLException {
        delegate.setRowId(parameterName, x);
    }

    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        delegate.setSQLXML(parameterIndex, xmlObject);
    }

    public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException {
        delegate.setSQLXML(parameterName, xmlObject);
    }

    public void setShort(int parameterIndex, short x) throws SQLException {
        delegate.setShort(parameterIndex, x);
    }

    public void setString(int parameterIndex, String x) throws SQLException {
        delegate.setString(parameterIndex, x);
    }

    public void setShort(String parameterName, short x) throws SQLException {
        delegate.setShort(parameterName, x);
    }

    public void setString(String parameterName, String x) throws SQLException {
        delegate.setString(parameterName, x);
    }

    public void setTime(String parameterName, Time x, Calendar cal) throws SQLException {
        delegate.setTime(parameterName, x, cal);
    }

    public void setTimestamp(String parameterName, Timestamp x, Calendar cal) throws SQLException {
        delegate.setTimestamp(parameterName, x, cal);
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return delegate.unwrap(iface);
    }

    public boolean wasNull() throws SQLException {
        return delegate.wasNull();
    }

    

}
