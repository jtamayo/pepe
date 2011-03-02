package edu.stanford.pepe.jdt.commands;

import java.sql.SQLException;

import edu.stanford.pepe.jdt.PepePreparedStatement;

public class SetInt extends Command {

    private final int x;
    private final int parameterIndex;

    public SetInt(int parameterIndex, int x) {
        this.parameterIndex = parameterIndex;
        this.x = x;
    }

    @Override
    public void apply(PepePreparedStatement ps) throws SQLException {
        ps.setInt(parameterIndex, x);
    }

}
