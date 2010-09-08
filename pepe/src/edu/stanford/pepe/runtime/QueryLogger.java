package edu.stanford.pepe.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import edu.stanford.pepe.runtime.lessmemory.LowFootprintQueryLogger;


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
	
	private static int i = 0;
	
	private static Set<StackTrace> traces = new HashSet<StackTrace>();
	
	public static synchronized void log(String sql, Set<Long> dependencies, long taint) {
		LowFootprintQueryLogger.log(sql, dependencies, taint);
		if (i > -29) return;
		final StackTraceElement[] stackTrace = new Throwable().getStackTrace();
		i++;
//		if (i++ > 250000) {
//			traces.add(new StackTrace(stackTrace));
//			System.out.print("[");
//			for (int i = stackTrace.length -1 ; i >= 0; i--) {
//				StackTraceElement element = stackTrace[i];
//				
//				System.out.print(toString(element));
//				if (i == 0) System.out.println("[");
//			}
//		} else {
//			if (i%1000 == 0) System.err.println(i);
//		}
		if (i++ < 250000) return;
		
		executions.putIfAbsent(taint, new QueryExecution(sql, stackTrace, taint, dependencies));
	}

	public static void consolidate() {
		System.out.println("Writing to disk");
		LowFootprintQueryLogger.toDisk();
		System.out.println("Done writing to disk");
		if (i > -200) return;
//		if (executions != null) {
//			Set<StackTrace> s = new HashSet<StackTrace>();
//			Collection<QueryExecution> allExecutions = executions.values();
//			for (QueryExecution execution : allExecutions) {
//				s.add(new StackTrace(execution.stackTrace));
//			}
//			
//			System.out.println("i: " + i);
//			System.out.println("Executions: " + executions.size());
//			System.out.println("Different queries: " + s.size());
////			final Set<Long> keySet = new TreeSet<Long>(executions.keySet());
////			for (Long key : keySet) {
////				System.out.printf("%16X\n",key);
////			}
//			
//			for (StackTrace trace : traces) {
//				StackTraceElement[] stackTrace = trace.stackTrace;
//				
//				System.out.print("[");
//				for (int i = stackTrace.length -1 ; i >= 0; i--) {
//					StackTraceElement element = stackTrace[i];
//					
//					System.out.print(toString(element));
//					if (i != 0) System.out.print("\t");
//				}
//				System.out.println("]");
//			}
//			
//			return;
//		}

//		// First, group the queries by transaction
//		final Map<Long,Collection<QueryExecution>> executionsPerTransactionId = new HashMap<Long, Collection<QueryExecution>>(executions.size() / 10); // guess around 10 ops/transaction
//		for (QueryExecution execution : executions.values()) {
//			final long transactionId = execution.transactionId;
//			Collection<QueryExecution> executionsInTransaction = executionsPerTransactionId.get(transactionId);
//			if (executionsInTransaction == null) {
//				executionsInTransaction = new ArrayList<QueryExecution>();
//				executionsPerTransactionId.put(transactionId, executionsInTransaction);
//			}
//			executionsInTransaction.add(execution);
//		}
//		
//		// Now, for each transaction, determine the operation to which they belong
//		final Map<StackTrace, Operation> operationsPerStackTraceSuffix = new HashMap<StackTrace, Operation>(); 
//		for (Collection<QueryExecution> collection : executionsPerTransactionId.values()) {
//			final StackTrace operationId = getSuffix(collection);
//			Operation op = operationsPerStackTraceSuffix.get(operationId);
//			if (op == null) {
//				op = new Operation(operationId, executions);
//				operationsPerStackTraceSuffix.put(operationId, op);
//			}
//			// And then, add all QueryExecutions in the transaction to the Operation
//			for (QueryExecution queryExecution : collection) {
//				op.addExecution(queryExecution);
//			}
//		}
//		
//		// Now print all operations
//		for (Operation o : operationsPerStackTraceSuffix.values()) {
//			System.out.println("-- Operation --");
//			o.print();
//		}
//		
//		for (Operation o : operationsPerStackTraceSuffix.values()) {
//			System.out.println(o.toGraph());
//		}
		
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

	public Query(StackTrace stackTrace) {
		this.id = new StackTrace(stackTrace);
	}

	public void addDependency(long otherTId, Query findQuery) {
		dependencies.add(findQuery.id);
	}

}

