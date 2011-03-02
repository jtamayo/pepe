package edu.stanford.pepe.jdt.commands;

import java.sql.SQLException;

import edu.stanford.pepe.jdt.PepePreparedStatement;

public class ClearParameters extends Command {

    @Override
    public void apply(PepePreparedStatement ps) throws SQLException {
        ps.clearParameters();
    }

}
