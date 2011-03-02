package edu.stanford.pepe.jdt.commands;

import java.sql.SQLException;

import edu.stanford.pepe.jdt.PepePreparedStatement;

public class SetEscapeProcessing extends Command {

    private final boolean enable;

    public SetEscapeProcessing(boolean enable) {
        this.enable = enable;
    }

    @Override
    public void apply(PepePreparedStatement ps) throws SQLException {
        ps.setEscapeProcessing(enable);
    }

}
