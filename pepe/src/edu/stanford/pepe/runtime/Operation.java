package edu.stanford.pepe.runtime;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import edu.stanford.pepe.postprocessing.Execution;

/**
 * An Operation represents a set of queries that, at a high level, are part of a
 * single operation. Every executed transaction is assumed to be an instance of
 * an Operation. In DayTrader, for example, operations would be "buy", "sell" or
 * "getMarketSummary".<br>
 * An Operation is identified by a StackTrace. The StackTrace is the common
 * stack prefix (or suffix, depending on how you count) shared by all queries in
 * the Operation.
 * 
 * @author jtamayo
 */
public class Operation {
	private final StackTrace id;
	
	private final Map<StackTrace, Query> queries = new HashMap<StackTrace, Query>();

	public Operation(StackTrace id) {
		this.id = id;
	}

	public StackTrace getId() {
		return id;
	}
	
	private Query findQuery(Execution execution) {
		final StackTrace key = new StackTrace(execution.getTrace());
		Query q = queries.get(key);
		if (q == null) {
			q = new Query(execution.getTrace());
			queries.put(key, q);
		}
		return q;
	}
	
	public static int getPrefixSize(final Collection<StackTrace> c) {
		final StackTrace prototype = c.iterator().next(); // get a query, all of the rest will be compared to this one
		for (int i = 0; i < prototype.stackTrace.length; i++) {
			for (StackTrace trace : c) {
				if (!prototype.stackTrace[i].equals(trace.stackTrace[i])) {
					return i;
				}
			}
		}
		return 0; // Mmm, shouldn't really happen, unless there's only one query
	}

	public static String toString(StackTraceElement element) {
		String className = element.getClassName().substring(Math.max(0, element.getClassName().lastIndexOf(".") + 1));
//		String fileName = element.getFileName();
		String methodName = element.getMethodName();
		int lineNumber = element.getLineNumber();

		return className
				+ "."
				+ methodName + "(" + lineNumber + ")";
//				+ (element.isNativeMethod() ? "(Native)" : (fileName != null && lineNumber >= 0 ? "(" + fileName
//						+ ":" + lineNumber + ")" : (fileName != null ? "(" + fileName + ")" : "(Unknown Source)")));
	}

	public String toGraph() {
		StringBuffer sb = new StringBuffer();
		sb.append("digraph " + this.hashCode() + " { \n");
		
		printContext(sb);
		// Layout properties
		sb.append("rankdir=RL;");
		sb.append("\n");
		
		// First assign a sequence number to each query
		int sequence = 1;
		Map<StackTrace, Integer> sequences = new HashMap<StackTrace, Integer>();
		if (!queries.isEmpty()) {
			for (StackTrace queryId : queries.keySet()) {
				sequences.put(queryId, sequence);
				sequence++;
			}
		}
		sequences.put(new StackTrace(new StackTraceElement[]{}), sequence++);
		// Now output all the nodes
		for (Query query : queries.values()) {
			outputNode(sb, query, sequences.get(query.getId()));
		}
		
		// Finally, add all the edges
		for (Query query : queries.values()) {
			for (Entry<StackTrace, Integer> dependency : query.getDependencyValues().entrySet()) {
				sb.append(sequences.get(query.getId()) + " -> " + sequences.get(dependency.getKey()) + " [label=\""
						+ dependency.getValue() + "/" + query.getExecutionCount() + "\"];\n");
			}
		}
		
		sb.append("}\n\n");
		return sb.toString();
	}

	// Print a description of the operation
	private void printContext(StringBuffer sb) {
		if (this.id.stackTrace.length > 0) {
			sb.append("label=<");
			sb.append("<font face=\"Times-Bold\">Context</font>");
			sb.append("<br/>");
			for (int i = this.id.stackTrace.length - 1; i >= 0; i--) {
				sb.append(toString(this.id.stackTrace[i]));
				if (i != 0) {
					sb.append("<br/>");
				}
			}
			
			sb.append(">;");
		}
	}

	private void outputNode(StringBuffer sb, Query query, final Integer querySequence) {
		sb.append(querySequence);
		// Node label
		sb.append(" [label=\"");
		final int start = query.getId().stackTrace.length - this.id.stackTrace.length - 1;
		final int end = Math.max(0, start - 4);
//		final int stackSize = Math.min(query.getId().stackTrace.length, this.id.stackTrace.length + 3);
		for (int i = start; i >= end; i--) {
			sb.append(toString(query.getId().stackTrace[i]));
			sb.append("\\n");
		}
		sb.append("-------------------\\n");
		sb.append("Execution Time: " + query.getAvgExecutionTime()/1e6 + "ms\\n");
		// Append selected/updated tables
		if (!query.getSelectedTables().isEmpty()) {
		    sb.append("Selected: ");
		    for (Entry<String, Integer> entry : query.getSelectedTables().entrySet()) {
                sb.append(entry.getKey() + "(" + entry.getValue() + ") ");
            }
		    sb.append("\\n");
		}
		if (!query.getUpdatedTables().isEmpty()) {
		    sb.append("Updated: ");
		    for (Entry<String, Integer> entry : query.getUpdatedTables().entrySet()) {
		        sb.append(entry.getKey() + "(" + entry.getValue() + ") ");
		    }
		    sb.append("\\n");
		}
		
		sb.append("\"");
		// Node shape
		sb.append(", shape=box");
		// Close the node
		sb.append("];\n");
	}

	public void addExecutions(Collection<Execution> collection) {
		Map<Long, Execution> executionsById = new HashMap<Long, Execution>();
		for (Execution execution : collection) {
			executionsById.put(execution.getId(), execution);
		}
		
		for (Execution execution : collection) {
			Query q = findQuery(execution);
			
			q.addExecution(execution);
			
			for (long dependency : execution.getDependencies()) {
				// iterate over all dependencies, then over all dependencies of each, check if they belong to the same transaction
				long otherTid = TransactionId.getTransactionId(dependency);
				if (otherTid < execution.getTransactionId()) {
					System.out.println("Query depends on previous transaction");
				} else if (otherTid == execution.getTransactionId()) {
					q.addDependency(findQuery(executionsById.get(dependency)));
				} else {
					// This means I depend on queries that started AFTER me. Whether intentional or not
					// we're showing this to the user
					System.out.println("Query depends on future transaction");
				}
			}
		}
	}
	
	
}
