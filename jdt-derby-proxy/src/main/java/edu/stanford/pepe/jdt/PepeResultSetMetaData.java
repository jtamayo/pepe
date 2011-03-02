package edu.stanford.pepe.jdt;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class PepeResultSetMetaData implements ResultSetMetaData {

    private final ResultSetMetaData delegate;

    public PepeResultSetMetaData(ResultSetMetaData delegate) {
        this.delegate = delegate;
    }

    @Override
    public int getColumnCount() throws SQLException {
        return delegate.getColumnCount();
    }

    @Override
    public boolean isAutoIncrement(int column) throws SQLException {
        return delegate.isAutoIncrement(column);
    }

    @Override
    public boolean isCaseSensitive(int column) throws SQLException {
        return delegate.isCaseSensitive(column);
    }

    @Override
    public boolean isSearchable(int column) throws SQLException {
        return delegate.isSearchable(column);
    }

    @Override
    public boolean isCurrency(int column) throws SQLException {
        return delegate.isCurrency(column);
    }

    @Override
    public int isNullable(int column) throws SQLException {
        return delegate.isNullable(column);
    }

    @Override
    public boolean isSigned(int column) throws SQLException {
        return delegate.isSigned(column);
    }

    @Override
    public int getColumnDisplaySize(int column) throws SQLException {
        return delegate.getColumnDisplaySize(column);
    }

    @Override
    public String getColumnLabel(int column) throws SQLException {
        return delegate.getColumnLabel(column);
    }

    @Override
    public String getColumnName(int column) throws SQLException {
        return delegate.getColumnName(column);
    }

    @Override
    public String getSchemaName(int column) throws SQLException {
        return delegate.getSchemaName(column);
    }

    @Override
    public int getPrecision(int column) throws SQLException {
        return delegate.getPrecision(column);
    }

    @Override
    public int getScale(int column) throws SQLException {
        return delegate.getScale(column);
    }

    @Override
    public String getTableName(int column) throws SQLException {
        return delegate.getTableName(column);
    }

    @Override
    public String getCatalogName(int column) throws SQLException {
        return delegate.getCatalogName(column);
    }

    @Override
    public int getColumnType(int column) throws SQLException {
        return delegate.getColumnType(column);
    }

    @Override
    public String getColumnTypeName(int column) throws SQLException {
        return delegate.getColumnTypeName(column);
    }

    @Override
    public boolean isReadOnly(int column) throws SQLException {
        return delegate.isReadOnly(column);
    }

    @Override
    public boolean isDefinitelyWritable(int column) throws SQLException {
        return delegate.isDefinitelyWritable(column);
    }

    @Override
    public String getColumnClassName(int column) throws SQLException {
        return delegate.getColumnClassName(column);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }

    @Override
    public boolean isWritable(int column) throws SQLException {
        return delegate.isWritable(column);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return delegate.unwrap(iface);
    }

}
