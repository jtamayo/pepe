package edu.stanford.pepe.jdt.commands;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

import edu.stanford.pepe.jdt.PepePreparedStatement;

public class SetTimestamp extends Command {

    private final int parameterIndex;
    private final Timestamp x;
    private final Calendar cal;

    public SetTimestamp(int parameterIndex, Timestamp x) {
        this.parameterIndex = parameterIndex;
        this.x = x;
        this.cal = null;
    }

    public SetTimestamp(int parameterIndex, Timestamp x, Calendar cal) {
        this.parameterIndex = parameterIndex;
        this.x = x;
        this.cal = cal;
    }

    @Override
    public void apply(PepePreparedStatement ps) throws SQLException {
        if (cal != null) {
            ps.setTimestamp(parameterIndex, x, cal);
        } else {
            ps.setTimestamp(parameterIndex, x);
        }
    }

}
