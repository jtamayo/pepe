package edu.stanford.pepe.newpostprocessing;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.stanford.pepe.newpostprocessing.Query.DependencyCount;
import edu.stanford.pepe.runtime.StackTrace;

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
    /**
     * Operations are identified by the commons suffix of all queries contained
     * in it.
     */
    private final StackTrace id;

    private final Map<StackTrace, Query> queries = new HashMap<StackTrace, Query>();

    public Operation(StackTrace id) {
        this.id = id;
    }

    public StackTrace getId() {
        return id;
    }

    private Query findQuery(final StackTrace key) {
        Query q = queries.get(key);
        if (q == null) {
            q = new Query(key);
            queries.put(key, q);
        }
        return q;
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
        sequences.put(new StackTrace(new StackTraceElement[] {}), sequence++);
        // Now output all the nodes
        for (Query query : queries.values()) {
            outputNode(sb, query, sequences.get(query.getId()));
        }

        // Finally, add all dependency edges
        for (Query query : queries.values()) {
            for (Entry<StackTrace, DependencyCount> entry : query.getDependencies().entrySet()) {
                final DependencyCount dependency = entry.getValue();
                final Integer thisSequence = sequences.get(query.getId());
                final Integer dependencySequence = sequences.get(entry.getKey());

                sb.append(thisSequence + " -> " + dependencySequence);
                sb.append(" [label=\"");
                sb.append(dependency.getType());
                sb.append("\"];\n");
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
        final int end = Math.max(0, start - 16);
        //		final int stackSize = Math.min(query.getId().stackTrace.length, this.id.stackTrace.length + 3);
        for (int i = start; i >= end; i--) {
            sb.append(toString(query.getId().stackTrace[i]));
            sb.append("\\n");
        }

        sb.append("\"");
        // Node shape
        sb.append(", shape=box");
        // Close the node
        sb.append("];\n");
    }

    public static String toString(StackTraceElement element) {
        String className = element.getClassName().substring(Math.max(0, element.getClassName().lastIndexOf(".") + 1));
        //      String fileName = element.getFileName();
        String methodName = element.getMethodName();
        int lineNumber = element.getLineNumber();

        return className + "." + methodName + "(" + lineNumber + ")";
        //              + (element.isNativeMethod() ? "(Native)" : (fileName != null && lineNumber >= 0 ? "(" + fileName
        //                      + ":" + lineNumber + ")" : (fileName != null ? "(" + fileName + ")" : "(Unknown Source)")));
    }
    
    /**
     * Adds to this operation all executions that belong to a single
     * transaction.
     */
    public void addTransaction(Map<StackTrace, Set<StackTrace>> dependencies, DependencyType type) {
        final Set<StackTrace> queryTraces = dependencies.keySet();

        for (StackTrace t : queryTraces ) {
            Query q = findQuery(t);
            for (StackTrace dependency : dependencies.get(t)) {
                q.addDependency(findQuery(dependency), type);
            }
        }
    }

    @Override
    public String toString() {
        return id.toString();
    }
    
}
