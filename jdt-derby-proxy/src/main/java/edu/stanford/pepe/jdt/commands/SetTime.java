package edu.stanford.pepe.jdt.commands;

import java.sql.SQLException;
import java.sql.Time;
import java.util.Calendar;

import edu.stanford.pepe.jdt.PepePreparedStatement;

public class SetTime extends Command {

    private final Time x;
    private final int parameterIndex;
    private final Calendar cal;

    public SetTime(int parameterIndex, Time x) {
        this.parameterIndex = parameterIndex;
        this.x = x;
        this.cal = null;
    }

    public SetTime(int parameterIndex, Time x, Calendar cal) {
        this.parameterIndex = parameterIndex;
        this.x = x;
        this.cal = cal;
    }

    @Override
    public void apply(PepePreparedStatement ps) throws SQLException {
        if (cal != null) {
            ps.setTime(parameterIndex, x, cal);
        } else {
            ps.setTime(parameterIndex, x);
        }
    }

}
