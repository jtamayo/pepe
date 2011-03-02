package edu.stanford.pepe.jdt.commands;

import java.sql.SQLException;

import edu.stanford.pepe.jdt.PepePreparedStatement;

public class SetFloat extends Command {

    private final int parameterIndex;
    private final float x;

    public SetFloat(int parameterIndex, float x) {
        this.parameterIndex = parameterIndex;
        this.x = x;
    }

    @Override
    public void apply(PepePreparedStatement ps) throws SQLException {
        ps.setFloat(parameterIndex,x);
    }

}
