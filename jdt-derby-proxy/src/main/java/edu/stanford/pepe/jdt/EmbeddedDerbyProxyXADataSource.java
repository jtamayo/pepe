package edu.stanford.pepe.jdt;

import java.sql.SQLException;

import javax.sql.XAConnection;
import javax.sql.XADataSource;

import org.apache.derby.jdbc.EmbeddedXADataSource;

public class EmbeddedDerbyProxyXADataSource extends EmbeddedDerbyProxyDataSource implements XADataSource {

    private final EmbeddedXADataSource delegate;
    
    public EmbeddedDerbyProxyXADataSource() {
        this(new EmbeddedXADataSource());
    }
    
    public EmbeddedDerbyProxyXADataSource(EmbeddedXADataSource delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    public XAConnection getXAConnection() throws SQLException {
        return null;
    }

    @Override
    public XAConnection getXAConnection(String user, String password) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

}
