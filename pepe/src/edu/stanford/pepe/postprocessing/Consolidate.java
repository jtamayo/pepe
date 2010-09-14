package edu.stanford.pepe.postprocessing;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import edu.stanford.pepe.runtime.Operation;
import edu.stanford.pepe.runtime.StackTrace;
import edu.stanford.pepe.runtime.lessmemory.IncompleteExecution;

public class Consolidate {
	/**
	 * @param args
	 * @throws Exception
	 * @throws
	 */
	public static void main(String[] args) throws Exception {
		String file = "/Users/juanmtamayo/Projects/pepe/dacapo/1284014174505.dmp";
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
		ConcurrentMap<StackTrace, List<IncompleteExecution>> executions = (ConcurrentMap<StackTrace, List<IncompleteExecution>>) ois
				.readObject();

		// First group the Executions by transactionId, creating full executions instead of incomplete ones.
		final Map<Long, Collection<Execution>> executionsPerTransactionId = new HashMap<Long, Collection<Execution>>(
				executions.size() / 10); // guess around 10 ops/transaction
		for (Entry<StackTrace, List<IncompleteExecution>> entry : executions.entrySet()) {
			StackTrace trace = entry.getKey();
			for (IncompleteExecution execution : entry.getValue()) {
				final Execution e = new Execution(execution.getDependencies(), execution.getId(), trace);
				final long transactionId = e.getTransactionId();
				if (!executionsPerTransactionId.containsKey(transactionId)) {
					executionsPerTransactionId.put(transactionId, new ArrayList<Execution>());
				}
				executionsPerTransactionId.get(transactionId).add(e);
			}
		}
		
		// Now, for each transaction, determine the operation to which they belong
		final Map<StackTrace, Operation> operationsPerStackTraceSuffix = new HashMap<StackTrace, Operation>(); 
		for (Collection<Execution> collection : executionsPerTransactionId.values()) {
			final StackTrace operationId = getSuffix(collection);
			Operation op = operationsPerStackTraceSuffix.get(operationId);
			if (op == null) {
				op = new Operation(operationId);
				operationsPerStackTraceSuffix.put(operationId, op);
			}
			op.addExecutions(collection);
		}
		
		// Now print all operations
		for (Operation o : operationsPerStackTraceSuffix.values()) {
			System.out.println("-- Operation --");
			o.print();
		}
		
		int index = 0;
		for (Operation o : operationsPerStackTraceSuffix.values()) {
			PrintWriter output = new PrintWriter("results" + File.separator + index + ".gv");
			index++;
			try {
				output.print(o.toGraph());
			} finally {
				output.close();
			}
		}
//		
//		for (StackTrace trace : executions.keySet()) {
//			print(trace);
//		}
	}

	public static void print(StackTrace trace) {
		for (int i = trace.stackTrace.length - 1; i >= 0 ; i--) {
			StackTraceElement e = trace.stackTrace[i];
			System.out.print(Operation.toString(e));
			if (i > 0) {
				System.out.print("\t");
			}
		}
		System.out.println();
	}
	
	private static StackTrace getSuffix(Collection<Execution> c) {
		final Execution prototype = c.iterator().next(); // get a query, all of the rest will be compared to this one
		for (int i = 0; i < prototype.getTrace().stackTrace.length; i++) {
			for (Execution query : c) {
				if (!prototype.getTrace().stackTrace[prototype.getTrace().stackTrace.length - i - 1]
						.equals(query.getTrace().stackTrace[query.getTrace().stackTrace.length - i - 1])) {
					// i is the length of the suffix
					StackTraceElement[] suffix = new StackTraceElement[i];
					System.arraycopy(prototype.getTrace().stackTrace, prototype.getTrace().stackTrace.length - i, suffix, 0, i);
					return new StackTrace(suffix);
				}
			}
		}
		return new StackTrace(new StackTraceElement[]{}); // There is only one query
	}

}
