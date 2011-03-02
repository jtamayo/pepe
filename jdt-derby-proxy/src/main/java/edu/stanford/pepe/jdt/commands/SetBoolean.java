package edu.stanford.pepe.jdt.commands;

import java.sql.SQLException;

import edu.stanford.pepe.jdt.PepePreparedStatement;

public class SetBoolean extends Command {

    private final boolean x;
    private final int parameterIndex;

    public SetBoolean(int parameterIndex, boolean x) {
        this.parameterIndex = parameterIndex;
        this.x = x;
    }

    @Override
    public void apply(PepePreparedStatement ps) throws SQLException {
        ps.setBoolean(parameterIndex, x);
    }

}
