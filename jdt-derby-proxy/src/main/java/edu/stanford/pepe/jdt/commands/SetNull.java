package edu.stanford.pepe.jdt.commands;

import java.sql.SQLException;

import edu.stanford.pepe.jdt.PepePreparedStatement;

public class SetNull extends Command {

    private final int sqlType;
    private final int parameterIndex;
    private final String typeName;

    public SetNull(int parameterIndex, int sqlType) {
        this.parameterIndex = parameterIndex;
        this.sqlType = sqlType;
        this.typeName = null;
    }

    public SetNull(int paramIndex, int sqlType, String typeName) {
        parameterIndex = paramIndex;
        this.sqlType = sqlType;
        this.typeName = typeName;
    }

    @Override
    public void apply(PepePreparedStatement ps) throws SQLException {
        if (typeName == null) {
            ps.setNull(parameterIndex, sqlType);
        } else {
            ps.setNull(parameterIndex, sqlType, typeName);
        }
    }

}
