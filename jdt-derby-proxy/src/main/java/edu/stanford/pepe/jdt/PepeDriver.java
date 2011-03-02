package edu.stanford.pepe.jdt;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PepeDriver implements Driver {

    public static final String JDBC_URL_PREFIX = "jdbc:pepe:";

    public static final int MAJOR_VERSION = 0;

    public static final int MINOR_VERSION = 1;

    private static Logger logger = Logger.getLogger("edu.stanford.pepe.jdbc");

    static {
        try {
            java.sql.DriverManager.registerDriver(new PepeDriver());
            logger.info("Registered driver");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error when registering driver", e);
            throw new RuntimeException(e);
        }
    }

    // Pepe implementation
    /**
     * Returns the delegate URL, stripping the PEPE prefix.
     */
    private String getDelegateUrl(String url) {
        return url.substring(JDBC_URL_PREFIX.length(), url.length());
    }

    // JDBC Interface
    @Override
    public PepeConnection connect(String url, Properties info) throws SQLException {
        logger.info("Connecting to " + url);
        if (acceptsURL(url)) {
            final String delegateUrl = getDelegateUrl(url);
            final Connection delegate = java.sql.DriverManager.getConnection(delegateUrl, info);
            return new PepeConnection(delegate);
        } else {
            return null;
        }
    }

    public boolean acceptsURL(String url) throws SQLException {
        return url != null && url.startsWith(JDBC_URL_PREFIX);
    }

    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        final String delegateUrl = getDelegateUrl(url);
        final Driver delegate = DriverManager.getDriver(delegateUrl);
        return delegate.getPropertyInfo(delegateUrl, info);
    }

    @Override
    public int getMajorVersion() {
        return MAJOR_VERSION;
    }

    @Override
    public int getMinorVersion() {
        return MINOR_VERSION;
    }

    @Override
    public boolean jdbcCompliant() {
        return true;
    }

}
