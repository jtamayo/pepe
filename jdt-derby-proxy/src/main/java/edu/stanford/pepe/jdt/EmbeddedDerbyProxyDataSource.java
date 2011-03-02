package edu.stanford.pepe.jdt;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.derby.jdbc.EmbeddedDataSource;

/**
 * Wrapper for a Derby EmbeddedDataSource object. It delegates everything to the
 * EmbeddedDataSource, except that it wraps the returned connections in a
 * PepeConnection object.
 * 
 * @author jtamayo
 */
public class EmbeddedDerbyProxyDataSource implements DataSource {

    private final EmbeddedDataSource delegate;
    
    static {
        System.out.println("Initializing class EmbeddedDerbyProxyDataSource");
    }

    public EmbeddedDerbyProxyDataSource() {
        this(new EmbeddedDataSource());
    }

    protected EmbeddedDerbyProxyDataSource(EmbeddedDataSource delegate) {
        this.delegate = delegate;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return delegate.getLogWriter();
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return delegate.getLoginTimeout();
    }

    @Override
    public void setLogWriter(PrintWriter arg0) throws SQLException {
        delegate.setLogWriter(arg0);
    }

    @Override
    public void setLoginTimeout(int arg0) throws SQLException {
        delegate.setLoginTimeout(arg0);
    }

    @Override
    public boolean isWrapperFor(Class<?> arg0) throws SQLException {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> arg0) throws SQLException {
        throw new UnsupportedOperationException("Wrapped DataSource cannot be accessed outside the proxy");
    }

    @Override
    public Connection getConnection() throws SQLException {
        System.out.println("Getting connection from DataSource");
        final Connection c = delegate.getConnection();
        return new PepeConnection(c);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        final Connection c = delegate.getConnection(username, password);
        return new PepeConnection(c);
    }

    public String getUser() {
        return delegate.getUser();
    }

    public void setUser(String user) {
        delegate.setUser(user);
    }

    public String getPassword() {
        return delegate.getPassword();
    }

    public void setPassword(String password) {
        delegate.setPassword(password);
    }

    public String getDescription() {
        return delegate.getDescription();
    }

    public void setDescription(String description) {
        delegate.setDescription(description);
    }

    public String getDataSourceName() {
        return delegate.getDataSourceName();
    }

    public void setDataSourceName(String name) {
        delegate.setDataSourceName(name);
    }

    public String getDatabaseName() {
        return delegate.getDatabaseName();
    }

    public void setDatabaseName(String name) {
        delegate.setDatabaseName(name);
    }

    public String getCreateDatabase() {
        return delegate.getCreateDatabase();
    }

    public void setCreateDatabase(String create) {
        delegate.setCreateDatabase(create);
    }

    public String getShutdownDatabase() {
        return delegate.getShutdownDatabase();
    }

    public void setShutdownDatabase(String shutdownDatabase) {
        delegate.setShutdownDatabase(shutdownDatabase);
    }

}
