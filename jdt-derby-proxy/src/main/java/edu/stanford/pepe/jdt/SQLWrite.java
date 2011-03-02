package edu.stanford.pepe.jdt;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.pepe.jdt.commands.Command;

/**
 * Represents an SQL Write operation, including inserts, updates and selects.
 * 
 * @author jtamayo
 */
public class SQLWrite {
    
    private final String sql;
    private final List<Command> commands;
    private final Type type;
    private StackTraceElement[] trace;

    public enum Type {
        EXECUTE_UPDATE, EXECUTE_BATCH, EXECUTE
    }

    public SQLWrite(String sql, List<Command> commands, Type type) {
        this.sql = sql;
        this.commands = new ArrayList<Command>(commands);
        this.type = type;
        this.trace = new Exception().getStackTrace().clone();
    }
    
    public StackTraceElement[] getTrace() {
        return trace;
    }

    /**
     * Executes this SQLWrite again against the given connection.
     * @throws SQLException 
     */
    public void execute(PepeConnection conn) throws SQLException {
        /*
         * Choices:
         * 1. Let the SQLWrite acess the underlying PreparedStatement from the PreparedStatement
         * 2. Create a new executeUpdateNoLog method in the PepePreparedStatement
         * 3. Let PepeConnection create non-wrapped PreparedStatements
         */
        PepePreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            for (Command c : commands) {
                c.apply(ps);
            }
            switch (type) {
            case EXECUTE_UPDATE:
                ps.executeUpdateNoLog();
                break;
            case EXECUTE_BATCH:
                ps.executeBatchNoLog();
                break;
            case EXECUTE:
                ps.executeNoLog();
                break;
            }
        } finally {
            if (ps != null) ps.close();
        }
    }

}
