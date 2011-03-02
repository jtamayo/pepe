package edu.stanford.pepe.jdt;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.pepe.jdt.commands.Command;

/**
 * A class that represents an SQL read (select) statement.
 * 
 * @author jtamayo
 */
public class SQLRead {

    private final String sql;
    private final List<Command> commands;
    private final StackTraceElement[] trace;

    public SQLRead(String sql, List<Command> commands) {
        this.sql = sql;
        this.commands = new ArrayList<Command>(commands);
        this.trace = new Exception().getStackTrace().clone();
    }

    public Fingerprint replay(PepeConnection conn) throws SQLException {
        PepePreparedStatement ps = null;
        PepeResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            for (Command c : commands) {
                c.apply(ps);
            }
            rs = ps.executeQueryNoLog();
            return computeFingerprint(rs);
        } finally {
            if (ps != null)
                ps.close();
            if (rs != null)
                rs.close();
        }
    }

    public StackTraceElement[] getTrace() {
        return trace;
    }
    
    private Fingerprint computeFingerprint(PepeResultSet rs) throws SQLException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final DataOutputStream os = new DataOutputStream(baos);
        final ResultSetMetaData metadata = rs.getMetaData();

        final int columns = metadata.getColumnCount();
        try {
            while (rs.next()) {
                os.writeUTF("<result>");
                for (int column = 1; column < columns; column++) {
                    os.writeUTF("<column" + column + ">");
                    write(rs, os, column, metadata.getColumnType(column));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Unexpected, who could be throwing an IO exception?",
                    e);
        }
        return computeFingerprint(baos);
    }

    private void write(PepeResultSet rs, final DataOutputStream os, int column, final int type) throws IOException,
            SQLException {
        // Temporary values, in case we get a null
        Date date;
        Time time;
        Timestamp timestamp;
        String string;
        BigDecimal bigDecimal;
        
        // TODO: Distinguish between null and zero for primitive returns.
        
        switch (type) {
        case Types.BIT:
        case Types.TINYINT:
        case Types.SMALLINT:
        case Types.INTEGER:
            os.writeInt(rs.getInt(column));
            break;
        case Types.BIGINT:
            os.writeLong(rs.getLong(column));
            break;
        case Types.FLOAT:
            os.writeFloat(rs.getFloat(column));
        case Types.REAL:
        case Types.DOUBLE:
            os.writeDouble(rs.getDouble(column));
            break;
        case Types.NUMERIC:
        case Types.DECIMAL:
            bigDecimal = rs.getBigDecimal(column);
            if (bigDecimal == null) {
                os.writeUTF("null");
            } else {
                os.writeUTF(bigDecimal.toString());
            }
            break;
        case Types.CHAR:
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
            string = rs.getString(column);
            if (string == null) {
                os.writeUTF("null");
            } else {
                os.writeUTF(string);
            }
            break;
        case Types.DATE:
            date = rs.getDate(column);
            if (date == null) {
                os.writeUTF("null");
            } else {
                os.writeLong(date.getTime());
            }
            break;
        case Types.TIME:
            time = rs.getTime(column);
            if (time == null) {
                os.writeUTF("null");
            } else {
                os.writeLong(time.getTime());
            }
            break;
        case Types.TIMESTAMP:
            timestamp = rs.getTimestamp(column);
            if (timestamp == null) {
                os.writeUTF("null");
            } else {
                os.writeLong(timestamp.getTime());
            }
            break;
        case Types.BINARY:
        case Types.VARBINARY:
        case Types.LONGVARBINARY:
        case Types.OTHER:
        case Types.JAVA_OBJECT:
        case Types.DISTINCT:
        case Types.STRUCT:
        case Types.ARRAY:
        case Types.BLOB:
        case Types.CLOB:
        case Types.REF:
        case Types.DATALINK:
            throw new UnsupportedOperationException("Type " + type + " not supported");
        case Types.NULL:
            os.writeUTF("NULL");
            break;
        case Types.BOOLEAN:
            os.writeBoolean(rs.getBoolean(column));
            break;
        default:
            throw new IllegalArgumentException("Given type " + type + " is not a known type");
        }
    }

    private Fingerprint computeFingerprint(ByteArrayOutputStream bout) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = md.digest(bout.toByteArray());
            return new Fingerprint(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Well, this is unexpected, no SHA-1?", e);
        }
    }

}
