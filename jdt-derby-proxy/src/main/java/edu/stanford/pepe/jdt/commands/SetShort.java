package edu.stanford.pepe.jdt.commands;

import java.sql.SQLException;

import edu.stanford.pepe.jdt.PepePreparedStatement;

public class SetShort extends Command {

    private final int parameterIndex;
    private final short x;

    public SetShort(int parameterIndex, short x) {
        this.parameterIndex = parameterIndex;
        this.x = x;
    }

    @Override
    public void apply(PepePreparedStatement ps) throws SQLException {
        ps.setShort(parameterIndex, x);
    }

}
