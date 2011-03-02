package edu.stanford.pepe.jdt.commands;

import java.sql.SQLException;

import edu.stanford.pepe.jdt.PepePreparedStatement;

public class SetMaxFieldSize extends Command {

    private final int max;

    public SetMaxFieldSize(int max) {
        this.max = max;
    }

    @Override
    public void apply(PepePreparedStatement ps) throws SQLException {
        ps.setMaxFieldSize(max);
    }

}
