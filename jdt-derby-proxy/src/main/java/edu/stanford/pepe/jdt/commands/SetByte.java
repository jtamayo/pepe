package edu.stanford.pepe.jdt.commands;

import java.sql.SQLException;

import edu.stanford.pepe.jdt.PepePreparedStatement;

public class SetByte extends Command {

    private final int parameterIndex;
    private final byte x;

    public SetByte(int parameterIndex, byte x) {
        this.parameterIndex = parameterIndex;
        this.x = x;
    }

    @Override
    public void apply(PepePreparedStatement ps) throws SQLException {
        ps.setByte(parameterIndex, x);
    }

}
