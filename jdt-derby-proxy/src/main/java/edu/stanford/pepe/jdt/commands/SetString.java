package edu.stanford.pepe.jdt.commands;

import java.sql.SQLException;

import edu.stanford.pepe.jdt.PepePreparedStatement;

public class SetString extends Command {

    private final int parameterIndex;
    private final String x;

    public SetString(int parameterIndex, String x) {
        this.parameterIndex = parameterIndex;
        this.x = x;
    }

    @Override
    public void apply(PepePreparedStatement ps) throws SQLException {
        ps.setString(parameterIndex, x);
    }

}
