package edu.stanford.pepe.jdt.commands;

import java.sql.Date;
import java.sql.SQLException;
import java.util.Calendar;

import edu.stanford.pepe.jdt.PepePreparedStatement;

public class SetDate extends Command {

    private final Date x;
    private final int parameterIndex;
    private final Calendar cal;

    public SetDate(int parameterIndex, Date x) {
        this.parameterIndex = parameterIndex;
        this.x = x;
        this.cal = null;
    }

    public SetDate(int parameterIndex, Date x, Calendar cal) {
        this.parameterIndex = parameterIndex;
        this.x = x;
        this.cal = cal;
    }

    @Override
    public void apply(PepePreparedStatement ps) throws SQLException {
        if (cal == null) {
            ps.setDate(parameterIndex, x);
        } else {
            ps.setDate(parameterIndex, x, cal);
        }
    }

}
