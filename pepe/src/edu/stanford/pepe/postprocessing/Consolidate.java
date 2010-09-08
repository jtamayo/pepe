package edu.stanford.pepe.postprocessing;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
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

	/*
	 * So, here's the problem I have a bunch of query executions, and I can
	 * somehow determine when they end.
	 * 
	 * Once again: I have the executions. They must be grouped by transaction,
	 * as only dependencies within a transaction make sense.
	 * 
	 * Now, to each transaction I can assign an "operation", which is a
	 * "high-level operation". The operation is the common prefix of the stack
	 * trace.
	 * 
	 * Then, I start iterating over the operations, and I build the dependency
	 * graph of the queries. To do so I need a reverse index from
	 * transactionId+queryId to StackTrace.
	 * 
	 * Definition of the dependency graph: - The nodes are "queries", which are
	 * identified by invocation site/stack trace. - The edges represent
	 * dependencies between the queries. - Dependencies are
	 * "number of transactions in which the queries were dependent" - I need to
	 * keep track of how many times the query was executed, so that I can
	 * display ratios - The graph will be stored as an adjacency list.
	 * 
	 * Algorithm: 1. Group executions by transaction. I need to build the full
	 * "execution" object, otherwise I'd loose the StackTrace. I need to keep
	 * track of when a transaction ends. The problem is, I have no way of
	 * realizing when a transaction ends. Unless, of course, they are in some
	 * sort of list. Too expensive, though, I cannot reliably mark them as
	 * completed.
	 * 
	 * Ok, then let's unpickle the objects so there's a shared StackTrace
	 * repository. Does that help? I need to iterate over all executions in a
	 * transaction because: - I need to determine to which operation the
	 * transaction belongs to. - Dependencies only make sense within a
	 * transaction. Also, it's easier to find a queryId+transactionId within a
	 * transaction, because the search space is smaller.
	 * 
	 * Ok, so let's say I grouped all executions in a transaction, and I can now
	 * iterate over them. Ok, awesome, then I can - Iterate over them to find
	 * the operation they belong to. - Then find, given the operation, append to
	 * the dependency graph the information in that transaction.
	 */

	/**
	 * @param args
	 * @throws Exception
	 * @throws
	 */
	public static void main(String[] args) throws Exception {
		String file = "/Users/juanmtamayo/Projects/pepe/dacapo/1283915076689.dmp";
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
		
		for (Operation o : operationsPerStackTraceSuffix.values()) {
			System.out.println(o.toGraph());
		}
	}
	
	private static StackTrace getSuffix(Collection<Execution> c) {
		final Execution prototype = c.iterator().next(); // get a query, all of the rest will be compared to this one
		for (int i = 0; i < prototype.getTrace().stackTrace.length; i++) {
			for (Execution query : c) {
				if (!prototype.getTrace().stackTrace[prototype.getTrace().stackTrace.length - i - 1]
						.equals(query.getTrace().stackTrace[query.getTrace().stackTrace.length - i - 1])) {
					// i is the length of the suffix
					StackTraceElement[] suffix = new StackTraceElement[i];
					System.arraycopy(prototype.getTrace().stackTrace, 0, suffix, 0, i);
					return new StackTrace(suffix);
				}
			}
		}
		return new StackTrace(new StackTraceElement[]{}); // There is only one query
	}

}
