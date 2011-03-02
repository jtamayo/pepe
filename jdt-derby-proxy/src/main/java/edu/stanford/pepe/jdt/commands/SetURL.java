package edu.stanford.pepe.jdt.commands;

import java.net.URL;
import java.sql.SQLException;

import edu.stanford.pepe.jdt.PepePreparedStatement;

public class SetURL extends Command {

    private final int parameterIndex;
    private final URL x;

    public SetURL(int parameterIndex, URL x) {
        this.parameterIndex = parameterIndex;
        this.x = x;
    }

    @Override
    public void apply(PepePreparedStatement ps) throws SQLException {
        ps.setURL(parameterIndex, x);
    }

}
