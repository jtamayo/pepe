package edu.stanford.pepe.jdt.commands;

import java.sql.SQLException;

import edu.stanford.pepe.jdt.PepePreparedStatement;

public class SetObject extends Command {

    private final int parameterIndex;
    private final Object x;
    private final Integer targetSqlType;
    private final Integer scale;

    public SetObject(int parameterIndex, Object x, int targetSqlType, int scale) {
        this.parameterIndex = parameterIndex;
        this.x = x;
        this.targetSqlType = targetSqlType;
        this.scale = scale;
    }

    public SetObject(int parameterIndex, Object x, int targetSqlType) {
        this.parameterIndex = parameterIndex;
        this.x = x;
        this.targetSqlType = targetSqlType;
        this.scale = null;
    }

    public SetObject(int parameterIndex, Object x) {
        this.parameterIndex = parameterIndex;
        this.x = x;
        this.targetSqlType = null;
        this.scale = null;
    }

    @Override
    public void apply(PepePreparedStatement ps) throws SQLException {
        if (scale != null && targetSqlType != null) {
            ps.setObject(parameterIndex, x, targetSqlType, scale);
        } else if (targetSqlType != null) {
            ps.setObject(parameterIndex, x, targetSqlType);
        } else {
            ps.setObject(parameterIndex, x);
        }
    }

}
