package edu.stanford.pepe.runtime;

import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents the execution of an SQL query; i.e. an invocation of
 * {@link PreparedStatement#executeQuery()} or
 * {@link PreparedStatement#executeUpdate()}
 * 
 * @author jtamayo
 */
public class QueryExecution {
	public final int queryId;
	public final long transactionId;
	public final StackTraceElement[] stackTrace;
	public final String sql;
	public final Set<Long> dependencies;

	public QueryExecution(String sql, StackTraceElement[] stackTrace, long taint, Set<Long> dependencies) {
		this.transactionId = TransactionId.getTransactionId(taint);
		this.queryId = TransactionId.getQueryId(taint);
		this.sql = sql;
		this.stackTrace = stackTrace;
		this.dependencies = new HashSet<Long>(dependencies.size());
		for (long l : dependencies) {
			// ignore dependencies to query 0, which really means no dependency
			if (l != 0) {
				this.dependencies.add(l);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Tx: " + Long.toHexString(transactionId) + " Q: " + queryId + " dep: ");
		for (long l : dependencies) {
			sb.append(Long.toHexString(l) + " ");
		}
		return sb.toString();
	}
}