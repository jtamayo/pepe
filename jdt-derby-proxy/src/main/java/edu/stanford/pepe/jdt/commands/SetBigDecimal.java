package edu.stanford.pepe.jdt.commands;

import java.math.BigDecimal;
import java.sql.SQLException;

import edu.stanford.pepe.jdt.PepePreparedStatement;

public class SetBigDecimal extends Command {

    private final int parameterIndex;
    private final BigDecimal x;

    public SetBigDecimal(int parameterIndex, BigDecimal x) {
        this.parameterIndex = parameterIndex;
        this.x = x;
    }

    @Override
    public void apply(PepePreparedStatement ps) throws SQLException {
        ps.setBigDecimal(parameterIndex, x);
    }

}
