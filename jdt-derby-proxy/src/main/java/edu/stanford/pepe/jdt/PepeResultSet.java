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
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

public class PepeResultSet implements ResultSet {

    private final ResultSet delegate;
    private final PepeAbstractStatement origin;

    public PepeResultSet(ResultSet delegate, PepeAbstractStatement origin) {
        this.delegate = delegate;
        this.origin = origin;
    }

    public boolean next() throws SQLException {
        return delegate.next();
    }

    public void close() throws SQLException {
        delegate.close();
    }

    public boolean getBoolean(int columnIndex) throws SQLException {
        return delegate.getBoolean(columnIndex);
    }

    public byte getByte(int columnIndex) throws SQLException {
        return delegate.getByte(columnIndex);
    }

    public float getFloat(int columnIndex) throws SQLException {
        return delegate.getFloat(columnIndex);
    }

    public double getDouble(int columnIndex) throws SQLException {
        return delegate.getDouble(columnIndex);
    }

    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        return delegate.getBigDecimal(columnIndex, scale);
    }

    public byte[] getBytes(int columnIndex) throws SQLException {
        return delegate.getBytes(columnIndex);
    }

    public Date getDate(int columnIndex) throws SQLException {
        return delegate.getDate(columnIndex);
    }

    public Time getTime(int columnIndex) throws SQLException {
        return delegate.getTime(columnIndex);
    }

    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        return delegate.getTimestamp(columnIndex);
    }

    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        return delegate.getAsciiStream(columnIndex);
    }

    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        return delegate.getUnicodeStream(columnIndex);
    }

    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        return delegate.getBinaryStream(columnIndex);
    }

    public String getString(String columnName) throws SQLException {
        return delegate.getString(columnName);
    }

    public boolean getBoolean(String columnName) throws SQLException {
        return delegate.getBoolean(columnName);
    }

    public byte getByte(String columnName) throws SQLException {
        return delegate.getByte(columnName);
    }

    public long getLong(String columnName) throws SQLException {
        return delegate.getLong(columnName);
    }

    public float getFloat(String columnName) throws SQLException {
        return delegate.getFloat(columnName);
    }

    public double getDouble(String columnName) throws SQLException {
        return delegate.getDouble(columnName);
    }

    public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
        return delegate.getBigDecimal(columnName, scale);
    }

    public byte[] getBytes(String columnName) throws SQLException {
        return delegate.getBytes(columnName);
    }

    public Date getDate(String columnName) throws SQLException {
        return delegate.getDate(columnName);
    }

    public Time getTime(String columnName) throws SQLException {
        return delegate.getTime(columnName);
    }

    public Timestamp getTimestamp(String columnName) throws SQLException {
        return delegate.getTimestamp(columnName);
    }

    public InputStream getAsciiStream(String columnName) throws SQLException {
        return delegate.getAsciiStream(columnName);
    }

    public InputStream getUnicodeStream(String columnName) throws SQLException {
        return delegate.getUnicodeStream(columnName);
    }

    public InputStream getBinaryStream(String columnName) throws SQLException {
        return delegate.getBinaryStream(columnName);
    }

    public SQLWarning getWarnings() throws SQLException {
        return delegate.getWarnings();
    }

    public void clearWarnings() throws SQLException {
        delegate.clearWarnings();
    }

    public String getCursorName() throws SQLException {
        return delegate.getCursorName();
    }

    public PepeResultSetMetaData getMetaData() throws SQLException {
        return new PepeResultSetMetaData(delegate.getMetaData());
    }

    public int findColumn(String columnName) throws SQLException {
        return delegate.findColumn(columnName);
    }

    public Reader getCharacterStream(int columnIndex) throws SQLException {
        return delegate.getCharacterStream(columnIndex);
    }

    public Reader getCharacterStream(String columnName) throws SQLException {
        return delegate.getCharacterStream(columnName);
    }

    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        return delegate.getBigDecimal(columnIndex);
    }

    public BigDecimal getBigDecimal(String columnName) throws SQLException {
        return delegate.getBigDecimal(columnName);
    }

    public void beforeFirst() throws SQLException {
        delegate.beforeFirst();
    }

    public void afterLast() throws SQLException {
        delegate.afterLast();
    }

    public boolean first() throws SQLException {
        return delegate.first();
    }

    public boolean absolute(int row) throws SQLException {
        return delegate.absolute(row);
    }

    public boolean relative(int rows) throws SQLException {
        return delegate.relative(rows);
    }

    public boolean previous() throws SQLException {
        return delegate.previous();
    }

    public void setFetchDirection(int direction) throws SQLException {
        delegate.setFetchDirection(direction);
    }

    public int getFetchDirection() throws SQLException {
        return delegate.getFetchDirection();
    }

    public void setFetchSize(int rows) throws SQLException {
        delegate.setFetchSize(rows);
    }

    public int getFetchSize() throws SQLException {
        return delegate.getFetchSize();
    }

    public int getConcurrency() throws SQLException {
        return delegate.getConcurrency();
    }

    public boolean rowUpdated() throws SQLException {
        return delegate.rowUpdated();
    }

    public boolean rowInserted() throws SQLException {
        return delegate.rowInserted();
    }

    public boolean rowDeleted() throws SQLException {
        return delegate.rowDeleted();
    }

    public void deleteRow() throws SQLException {
        delegate.deleteRow();
    }

    public void refreshRow() throws SQLException {
        delegate.refreshRow();
    }

    public void cancelRowUpdates() throws SQLException {
        delegate.cancelRowUpdates();
    }

    public void moveToInsertRow() throws SQLException {
        delegate.moveToInsertRow();
    }

    public Blob getBlob(int i) throws SQLException {
        return delegate.getBlob(i);
    }

    public Clob getClob(int i) throws SQLException {
        return delegate.getClob(i);
    }

    public Array getArray(int i) throws SQLException {
        return delegate.getArray(i);
    }

    public Blob getBlob(String colName) throws SQLException {
        return delegate.getBlob(colName);
    }

    public Clob getClob(String colName) throws SQLException {
        return delegate.getClob(colName);
    }

    public Array getArray(String colName) throws SQLException {
        return delegate.getArray(colName);
    }

    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        return delegate.getDate(columnIndex, cal);
    }

    public Date getDate(String columnName, Calendar cal) throws SQLException {
        return delegate.getDate(columnName, cal);
    }

    public int getHoldability() throws SQLException {
        return delegate.getHoldability();
    }

    public int getInt(int columnIndex) throws SQLException {
        return delegate.getInt(columnIndex);
    }

    public long getLong(int columnIndex) throws SQLException {
        return delegate.getLong(columnIndex);
    }

    public int getInt(String columnName) throws SQLException {
        return delegate.getInt(columnName);
    }

    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        return delegate.getNCharacterStream(columnIndex);
    }

    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        return delegate.getNCharacterStream(columnLabel);
    }

    public NClob getNClob(int columnIndex) throws SQLException {
        return delegate.getNClob(columnIndex);
    }

    public NClob getNClob(String columnLabel) throws SQLException {
        return delegate.getNClob(columnLabel);
    }

    public String getNString(int columnIndex) throws SQLException {
        return delegate.getNString(columnIndex);
    }

    public String getNString(String columnLabel) throws SQLException {
        return delegate.getNString(columnLabel);
    }

    public Object getObject(int columnIndex) throws SQLException {
        return delegate.getObject(columnIndex);
    }

    public Object getObject(String columnName) throws SQLException {
        return delegate.getObject(columnName);
    }

    public boolean isBeforeFirst() throws SQLException {
        return delegate.isBeforeFirst();
    }

    public boolean isAfterLast() throws SQLException {
        return delegate.isAfterLast();
    }

    public int getRow() throws SQLException {
        return delegate.getRow();
    }

    public int getType() throws SQLException {
        return delegate.getType();
    }

    public void insertRow() throws SQLException {
        delegate.insertRow();
    }

    public Object getObject(int i, Map<String, Class<?>> map) throws SQLException {
        return delegate.getObject(i, map);
    }

    public Ref getRef(int i) throws SQLException {
        return delegate.getRef(i);
    }

    public Object getObject(String colName, Map<String, Class<?>> map) throws SQLException {
        return delegate.getObject(colName, map);
    }

    public Ref getRef(String colName) throws SQLException {
        return delegate.getRef(colName);
    }

    public RowId getRowId(int columnIndex) throws SQLException {
        return delegate.getRowId(columnIndex);
    }

    public RowId getRowId(String columnLabel) throws SQLException {
        return delegate.getRowId(columnLabel);
    }

    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        return delegate.getSQLXML(columnIndex);
    }

    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        return delegate.getSQLXML(columnLabel);
    }

    public String getString(int columnIndex) throws SQLException {
        return delegate.getString(columnIndex);
    }

    public short getShort(int columnIndex) throws SQLException {
        return delegate.getShort(columnIndex);
    }

    public short getShort(String columnName) throws SQLException {
        return delegate.getShort(columnName);
    }

    public PepeAbstractStatement getStatement() throws SQLException {
        return this.origin;
    }

    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        return delegate.getTime(columnIndex, cal);
    }

    public Time getTime(String columnName, Calendar cal) throws SQLException {
        return delegate.getTime(columnName, cal);
    }

    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        return delegate.getTimestamp(columnIndex, cal);
    }

    public Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException {
        return delegate.getTimestamp(columnName, cal);
    }

    public URL getURL(int columnIndex) throws SQLException {
        return delegate.getURL(columnIndex);
    }

    public URL getURL(String columnName) throws SQLException {
        return delegate.getURL(columnName);
    }

    public boolean isClosed() throws SQLException {
        return delegate.isClosed();
    }

    public boolean isFirst() throws SQLException {
        return delegate.isFirst();
    }

    public boolean isLast() throws SQLException {
        return delegate.isLast();
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }

    public boolean last() throws SQLException {
        return delegate.last();
    }

    public void moveToCurrentRow() throws SQLException {
        delegate.moveToCurrentRow();
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return delegate.unwrap(iface);
    }

    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
        delegate.updateAsciiStream(columnIndex, x, length);
    }

    public void updateArray(int columnIndex, Array x) throws SQLException {
        delegate.updateArray(columnIndex, x);
    }

    public void updateArray(String columnName, Array x) throws SQLException {
        delegate.updateArray(columnName, x);
    }

    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
        delegate.updateAsciiStream(columnIndex, x, length);
    }

    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
        delegate.updateAsciiStream(columnIndex, x);
    }

    public void updateAsciiStream(String columnName, InputStream x, int length) throws SQLException {
        delegate.updateAsciiStream(columnName, x, length);
    }

    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
        delegate.updateAsciiStream(columnLabel, x, length);
    }

    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
        delegate.updateAsciiStream(columnLabel, x);
    }

    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        delegate.updateBigDecimal(columnIndex, x);
    }

    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
        delegate.updateBinaryStream(columnIndex, x, length);
    }

    public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
        delegate.updateBigDecimal(columnName, x);
    }

    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
        delegate.updateBinaryStream(columnIndex, x, length);
    }

    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
        delegate.updateBinaryStream(columnIndex, x);
    }

    public void updateByte(int columnIndex, byte x) throws SQLException {
        delegate.updateByte(columnIndex, x);
    }

    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
        delegate.updateBytes(columnIndex, x);
    }

    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
        delegate.updateCharacterStream(columnIndex, x, length);
    }

    public void updateByte(String columnName, byte x) throws SQLException {
        delegate.updateByte(columnName, x);
    }

    public void updateBytes(String columnName, byte[] x) throws SQLException {
        delegate.updateBytes(columnName, x);
    }

    public void updateBinaryStream(String columnName, InputStream x, int length) throws SQLException {
        delegate.updateBinaryStream(columnName, x, length);
    }

    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
        delegate.updateBinaryStream(columnLabel, x, length);
    }

    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
        delegate.updateBinaryStream(columnLabel, x);
    }

    public void updateBlob(int columnIndex, Blob x) throws SQLException {
        delegate.updateBlob(columnIndex, x);
    }

    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
        delegate.updateBlob(columnIndex, inputStream, length);
    }

    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
        delegate.updateBlob(columnIndex, inputStream);
    }

    public void updateBlob(String columnName, Blob x) throws SQLException {
        delegate.updateBlob(columnName, x);
    }

    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
        delegate.updateBlob(columnLabel, inputStream, length);
    }

    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
        delegate.updateBlob(columnLabel, inputStream);
    }

    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        delegate.updateBoolean(columnIndex, x);
    }

    public void updateBoolean(String columnName, boolean x) throws SQLException {
        delegate.updateBoolean(columnName, x);
    }

    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        delegate.updateCharacterStream(columnIndex, x, length);
    }

    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
        delegate.updateCharacterStream(columnIndex, x);
    }

    public void updateCharacterStream(String columnName, Reader reader, int length) throws SQLException {
        delegate.updateCharacterStream(columnName, reader, length);
    }

    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        delegate.updateCharacterStream(columnLabel, reader, length);
    }

    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
        delegate.updateCharacterStream(columnLabel, reader);
    }

    public void updateClob(int columnIndex, Clob x) throws SQLException {
        delegate.updateClob(columnIndex, x);
    }

    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
        delegate.updateClob(columnIndex, reader, length);
    }

    public void updateClob(int columnIndex, Reader reader) throws SQLException {
        delegate.updateClob(columnIndex, reader);
    }

    public void updateInt(int columnIndex, int x) throws SQLException {
        delegate.updateInt(columnIndex, x);
    }

    public void updateLong(int columnIndex, long x) throws SQLException {
        delegate.updateLong(columnIndex, x);
    }

    public void updateFloat(int columnIndex, float x) throws SQLException {
        delegate.updateFloat(columnIndex, x);
    }

    public void updateDouble(int columnIndex, double x) throws SQLException {
        delegate.updateDouble(columnIndex, x);
    }

    public void updateInt(String columnName, int x) throws SQLException {
        delegate.updateInt(columnName, x);
    }

    public void updateLong(String columnName, long x) throws SQLException {
        delegate.updateLong(columnName, x);
    }

    public void updateFloat(String columnName, float x) throws SQLException {
        delegate.updateFloat(columnName, x);
    }

    public void updateDouble(String columnName, double x) throws SQLException {
        delegate.updateDouble(columnName, x);
    }

    public void updateClob(String columnName, Clob x) throws SQLException {
        delegate.updateClob(columnName, x);
    }

    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
        delegate.updateClob(columnLabel, reader, length);
    }

    public void updateClob(String columnLabel, Reader reader) throws SQLException {
        delegate.updateClob(columnLabel, reader);
    }

    public void updateDate(int columnIndex, Date x) throws SQLException {
        delegate.updateDate(columnIndex, x);
    }

    public void updateDate(String columnName, Date x) throws SQLException {
        delegate.updateDate(columnName, x);
    }

    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        delegate.updateNCharacterStream(columnIndex, x, length);
    }

    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
        delegate.updateNCharacterStream(columnIndex, x);
    }

    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        delegate.updateNCharacterStream(columnLabel, reader, length);
    }

    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
        delegate.updateNCharacterStream(columnLabel, reader);
    }

    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
        delegate.updateNClob(columnIndex, nClob);
    }

    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
        delegate.updateNClob(columnIndex, reader, length);
    }

    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
        delegate.updateNClob(columnIndex, reader);
    }

    public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
        delegate.updateNClob(columnLabel, nClob);
    }

    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
        delegate.updateNClob(columnLabel, reader, length);
    }

    public void updateNClob(String columnLabel, Reader reader) throws SQLException {
        delegate.updateNClob(columnLabel, reader);
    }

    public void updateNString(int columnIndex, String nString) throws SQLException {
        delegate.updateNString(columnIndex, nString);
    }

    public void updateNString(String columnLabel, String nString) throws SQLException {
        delegate.updateNString(columnLabel, nString);
    }

    public void updateNull(int columnIndex) throws SQLException {
        delegate.updateNull(columnIndex);
    }

    public void updateObject(int columnIndex, Object x, int scale) throws SQLException {
        delegate.updateObject(columnIndex, x, scale);
    }

    public void updateObject(int columnIndex, Object x) throws SQLException {
        delegate.updateObject(columnIndex, x);
    }

    public void updateNull(String columnName) throws SQLException {
        delegate.updateNull(columnName);
    }

    public void updateObject(String columnName, Object x, int scale) throws SQLException {
        delegate.updateObject(columnName, x, scale);
    }

    public void updateObject(String columnName, Object x) throws SQLException {
        delegate.updateObject(columnName, x);
    }

    public void updateRow() throws SQLException {
        delegate.updateRow();
    }

    public void updateRef(int columnIndex, Ref x) throws SQLException {
        delegate.updateRef(columnIndex, x);
    }

    public void updateRef(String columnName, Ref x) throws SQLException {
        delegate.updateRef(columnName, x);
    }

    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        delegate.updateRowId(columnIndex, x);
    }

    public void updateRowId(String columnLabel, RowId x) throws SQLException {
        delegate.updateRowId(columnLabel, x);
    }

    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
        delegate.updateSQLXML(columnIndex, xmlObject);
    }

    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
        delegate.updateSQLXML(columnLabel, xmlObject);
    }

    public boolean wasNull() throws SQLException {
        return delegate.wasNull();
    }

    public void updateShort(int columnIndex, short x) throws SQLException {
        delegate.updateShort(columnIndex, x);
    }

    public void updateString(int columnIndex, String x) throws SQLException {
        delegate.updateString(columnIndex, x);
    }

    public void updateTime(int columnIndex, Time x) throws SQLException {
        delegate.updateTime(columnIndex, x);
    }

    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
        delegate.updateTimestamp(columnIndex, x);
    }

    public void updateShort(String columnName, short x) throws SQLException {
        delegate.updateShort(columnName, x);
    }

    public void updateString(String columnName, String x) throws SQLException {
        delegate.updateString(columnName, x);
    }

    public void updateTime(String columnName, Time x) throws SQLException {
        delegate.updateTime(columnName, x);
    }

    public void updateTimestamp(String columnName, Timestamp x) throws SQLException {
        delegate.updateTimestamp(columnName, x);
    }
    
    

}
