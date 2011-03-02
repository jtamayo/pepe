package edu.stanford.pepe.jdt.commands;

import java.sql.SQLException;

import edu.stanford.pepe.jdt.PepePreparedStatement;

public class SetLong extends Command {

    private final int parameterIndex;
    private final long x;

    public SetLong(int parameterIndex, long x) {
        this.parameterIndex = parameterIndex;
        this.x = x;
    }

    @Override
    public void apply(PepePreparedStatement ps) throws SQLException {
        ps.setLong(parameterIndex, x);
    }

}
