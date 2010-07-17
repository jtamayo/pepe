package edu.stanford.pepe.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class QueryLogger {
	private static ConcurrentMap<Long, QueryExecution> executions = new ConcurrentHashMap<Long, QueryExecution>();

	
	static {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				consolidate();
			}
		});
	}
	
	public static void log(String sql, Set<Long> dependencies, long taint) {
		executions.putIfAbsent(taint, new QueryExecution(sql, new Throwable().getStackTrace(), taint, dependencies));
	}

	public static void consolidate() {
		if (executions != null) {
			Set<StackTrace> s = new HashSet<StackTrace>();
			Collection<QueryExecution> allExecutions = executions.values();
			for (QueryExecution execution : allExecutions) {
				s.add(new StackTrace(execution.stackTrace));
			}
			for (StackTrace stackTrace : s) {
				System.out.print("[");
				for (int i = stackTrace.stackTrace.length - 1; i >= 0; i--) {
					System.out.print(stackTrace.stackTrace[i]);
					if (i != 0) System.out.print("\t");
				}
				System.out.println("]");
			}
			return;
		}
		// First, group the queries by transaction
		final Map<Long,Collection<QueryExecution>> executionsPerTransactionId = new HashMap<Long, Collection<QueryExecution>>(executions.size() / 10); // guess around 10 ops/transaction
		for (QueryExecution execution : executions.values()) {
			final long transactionId = execution.transactionId;
			Collection<QueryExecution> executionsInTransaction = executionsPerTransactionId.get(transactionId);
			if (executionsInTransaction == null) {
				executionsInTransaction = new ArrayList<QueryExecution>();
				executionsPerTransactionId.put(transactionId, executionsInTransaction);
			}
			executionsInTransaction.add(execution);
		}
		
		// Now, for each transaction, determine the operation to which they belong
		final Map<StackTrace, Operation> operationsPerStackTraceSuffix = new HashMap<StackTrace, Operation>(); 
		for (Collection<QueryExecution> collection : executionsPerTransactionId.values()) {
			final StackTrace operationId = getSuffix(collection);
			Operation op = operationsPerStackTraceSuffix.get(operationId);
			if (op == null) {
				op = new Operation(operationId, executions);
				operationsPerStackTraceSuffix.put(operationId, op);
			}
			// And then, add all QueryExecutions in the transaction to the Operation
			for (QueryExecution queryExecution : collection) {
				op.addExecution(queryExecution);
			}
		}
		
		// Now print all operations
		for (Operation o : operationsPerStackTraceSuffix.values()) {
			System.out.println("-- Operation --");
			o.print();
		}
		
		// Now, I have built all the dependencies in the queryId, I need to somehow print the graph
	}

	private static StackTrace getSuffix(Collection<QueryExecution> c) {
		final QueryExecution prototype = c.iterator().next(); // get a query, all of the rest will be compared to this one
		for (int i = 0; i < prototype.stackTrace.length; i++) {
			for (QueryExecution query : c) {
				if (!prototype.stackTrace[prototype.stackTrace.length - i - 1]
						.equals(query.stackTrace[query.stackTrace.length - i - 1])) {
					// i is the length of the suffix
					StackTraceElement[] suffix = new StackTraceElement[i];
					System.arraycopy(prototype.stackTrace, 0, suffix, 0, i);
					return new StackTrace(suffix);
				}
			}
		}
		return new StackTrace(new StackTraceElement[]{}); // There is only one query
	}
}

/**
 * A query represents a specific call point in the code, as defined by the stack
 * trace of the point where the SQL query is invoked.
 * 
 * @author jtamayo
 */
class Query {
	public final StackTrace id;
	public final Set<StackTrace> dependencies = new HashSet<StackTrace>();

	public Query(StackTraceElement[] stackTrace) {
		this.id = new StackTrace(stackTrace);
	}

	public void addDependency(long otherTId, Query findQuery) {
		dependencies.add(findQuery.id);
	}

}

