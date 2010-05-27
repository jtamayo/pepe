package util;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import oracle.jdbc.pool.OracleDataSource;


public class Util {

	public static Connection getOracleConnection() throws SQLException {
		OracleDataSource ods = new OracleDataSource();
//		ods.setURL("jdbc:oracle:thin:hr/hr@localhost:1521/XE");
		ods.setURL("jdbc:oracle:thin:hr/hr@picasso:1521/XE");
		final Connection conn = ods.getConnection();
		conn.setAutoCommit(false);
		return conn;
	}

	public static Connection getMySqlConnection() throws Exception {
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		return DriverManager.getConnection("jdbc:mysql://localhost/wiki?user=root&password=jaramillo");
	}

}
