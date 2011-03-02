package edu.stanford.pepe.jdt.commands;

import java.sql.SQLException;

import edu.stanford.pepe.jdt.PepePreparedStatement;

/**
 * Abstract class that represents invoking a method on a PreparedStatement.
 * 
 * @author jtamayo
 */
public abstract class Command {
    public abstract void apply(PepePreparedStatement ps) throws SQLException;
}
