package edu.stanford.pepe.runtime;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import edu.stanford.pepe.runtime.Query.QueryId;

public class QueryLogger {
	private static ConcurrentMap<Long, QueryExecution> executions = new ConcurrentHashMap<Long, QueryExecution>();

	private static Map<Query.QueryId, Query> queries = new HashMap<QueryId, Query>();// TODO: Initialize this with a reasonable initial size
	
	static {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				compile();
			}
		});
	}
	
	public static void log(String sql, Set<Long> dependencies, long taint) {
		executions.putIfAbsent(taint, new QueryExecution(sql, new Throwable().getStackTrace(), taint, dependencies));
	}

	public static void compile() {
		for (QueryExecution execution : executions.values()) {
			// First find the Query for a certain instance
			Query q = findQuery(execution);
			for (long l : execution.dependencies) {
				// iterate over all dependencies, then over all dependencies of each, check if they belong to the same transaction... fuck
				// Dependencies are to be analyzed inside a transaction, not out of it.
				long otherTId = TransactionId.getTransactionId(l);
				if (otherTId < execution.transactionId) {
					// If I depend on data from a previous transaction, I don't care, it could be static
					// data retrieved at the beginning of execution
					System.out.println("Query depends on previous transaction");
				} else if (otherTId == execution.transactionId) {
					// For queries in the same transaction, add the dependencies to the query
					// Here I should have some sort of iterator, that for a given long gives me all other longs it depends on.
					List<Long> dependencyIds = TransactionId.getDependencies(l);
					for (long otherExecution : dependencyIds) {
						QueryExecution exec = executions.get(otherExecution);
						q.addDependency(otherTId, findQuery(exec));
					}
				} else {
					// This means I depend on queries that started AFTER me. Whether intentional or not
					// we're showing this to the user
					System.out.println("Query depends on future transaction");
				}
			}
		}
		// Now, I have built all the dependencies in the queryId, I need to somehow print the graph
		int sequence = 1;
		System.out.println();
		System.out.println("-- Queries --");
		Map<QueryId, Integer> sequences = new HashMap<QueryId, Integer>();
		if (!queries.isEmpty()) {
			int prefix = getStackPrefixSize();
			int suffix = getStackSuffixSize();
			for (QueryId queryId : queries.keySet()) {
				sequences.put(queryId, sequence);
				System.out.println(sequence + ": " + getShortDescription(queryId, prefix, suffix));
				sequence++;
			}
		}
		System.out.println();
		System.out.println("-- Dependencies --");
		for (Entry<QueryId, Query> entry : queries.entrySet()) {
			System.out.print(sequences.get(entry.getKey()) + ": ");
			for (QueryId dependency : entry.getValue().dependencies) {
				System.out.print(sequences.get(dependency) + " ");
			}
			System.out.println();
		}
	}

	private static String getShortDescription(QueryId queryId, int prefixLength, int suffix) {
		StringBuilder sb = new StringBuilder();
		for (int i = queryId.stackTrace.length - suffix - 1; i >= prefixLength; i--) {
			StackTraceElement element = queryId.stackTrace[i];
			sb.append(toString(element));
			if (i != queryId.stackTrace.length - 1) { 
				sb.append(" > ");
			}
		}
//		for (int i = prefixLength; i < queryId.stackTrace.length - suffix; i++) {
//			StackTraceElement element = queryId.stackTrace[i];
//			sb.append(toString(element));
//			if (i != queryId.stackTrace.length - 1) { 
//				sb.append(" < ");
//			}
//		}
		return sb.toString();
	}
	
	private static String toString(StackTraceElement element) {
		String className = element.getClassName().substring(Math.max(0, element.getClassName().lastIndexOf(".") + 1));
		String fileName = element.getFileName();
		String methodName = element.getMethodName();
		int lineNumber = element.getLineNumber();
		
        return className + "." + methodName +
		        (element.isNativeMethod() ? "(Native Method)" :
		         (fileName != null && lineNumber >= 0 ?
		          "(" + fileName + ":" + lineNumber + ")" :
		          (fileName != null ?  "("+fileName+")" : "(Unknown Source)")));
	}

	/*
	 * Loops through all the queries, and determines how many StackTraceElement are common
	 * to all of them. Common StackTraceElement can safely be ignored.
	 */
	private static int getStackPrefixSize() {
		final QueryId prototype = queries.keySet().iterator().next(); // get a query, all of the rest will be compared to this one
		for (int i = 0; i < prototype.stackTrace.length; i++) {
			for (QueryId query : queries.keySet()) {
				if (!prototype.stackTrace[i].equals(query.stackTrace[i])) {
					return i;
				}
			}
		}
		return prototype.stackTrace.length; // Mmm, shouldn't really happen, unless there's only one query
	}
	
	private static int getStackSuffixSize() {
		final QueryId prototype = queries.keySet().iterator().next(); // get a query, all of the rest will be compared to this one
		for (int i = 0; i < prototype.stackTrace.length; i++) {
			for (QueryId query : queries.keySet()) {
				if (!prototype.stackTrace[prototype.stackTrace.length - i - 1]
						.equals(query.stackTrace[query.stackTrace.length - i - 1])) {
					return i;
				}
			}
		}
		return 0; // Mmm, shouldn't really happen, unless there's only one query, in which case we want to display all of it
	}

	private static Query findQuery(QueryExecution execution) {
		final Query.QueryId key = new Query.QueryId(execution.stackTrace);
		Query q = queries.get(key);
		if (q == null) {
			q = new Query(execution.stackTrace);
			queries.put(key, q);
		}
		return q;
	}

}

/**
 * A query represents a specific call point in the code, as defined by the stack
 * trace of the point where the SQL query is invoked.
 * 
 * @author jtamayo
 */
class Query {
	public final QueryId id;
	public final Set<QueryId> dependencies = new HashSet<QueryId>();

	public Query(StackTraceElement[] stackTrace) {
		this.id = new QueryId(stackTrace);
	}

	public void addDependency(long otherTId, Query findQuery) {
		dependencies.add(findQuery.id);
	}

	public static class QueryId {
		public final StackTraceElement[] stackTrace;

		public QueryId(StackTraceElement[] stackTrace) {
			this.stackTrace = stackTrace.clone();
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof QueryId)) {
				return false;
			}
			QueryId other = (QueryId) obj;
			if (other.stackTrace.length != this.stackTrace.length) {
				return false;
			}
			for (int i = 0; i < stackTrace.length; i++) {
				if (!other.stackTrace[i].equals(this.stackTrace[i])) {
					return false;
				}
			}
			return true;
		}

		@Override
		public int hashCode() {
			int hashcode = 0;
			for (StackTraceElement element : stackTrace) {
				hashcode += 31*element.hashCode();
			}
			return hashcode;
		}
	}

}

class QueryExecution {
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
