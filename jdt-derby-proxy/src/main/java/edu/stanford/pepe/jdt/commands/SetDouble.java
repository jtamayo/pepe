package edu.stanford.pepe.jdt.commands;

import java.sql.SQLException;

import edu.stanford.pepe.jdt.PepePreparedStatement;

public class SetDouble extends Command {

    private final double x;
    private final int parameterIndex;

    public SetDouble(int parameterIndex, double x) {
        this.parameterIndex = parameterIndex;
        this.x = x;
    }

    @Override
    public void apply(PepePreparedStatement ps) throws SQLException {
        ps.setDouble(parameterIndex, x);
    }

}
