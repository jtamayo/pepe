package edu.stanford.pepe.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.stanford.pepe.postprocessing.Execution;
import edu.stanford.pepe.runtime.Query.Dependency;

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
    private final Map<List<StackTrace>, Integer> executionOrders = new HashMap<List<StackTrace>, Integer>();

    public Operation(StackTrace id) {
        this.id = id;
    }

    public StackTrace getId() {
        return id;
    }

    private Query findQuery(Execution execution) {
        final StackTrace key = new StackTrace(execution.getTrace());
        return findQuery(key);
    }

    private Query findQuery(final StackTrace key) {
        Query q = queries.get(key);
        if (q == null) {
            q = new Query(key);
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

        return className + "." + methodName + "(" + lineNumber + ")";
        //				+ (element.isNativeMethod() ? "(Native)" : (fileName != null && lineNumber >= 0 ? "(" + fileName
        //						+ ":" + lineNumber + ")" : (fileName != null ? "(" + fileName + ")" : "(Unknown Source)")));
    }

    public String toGraph() {
	//XXX Hack to get a list of SQL statements
	System.out.println("=== OPERATION ===");
        for (int i = this.id.stackTrace.length - 1; i >= 0; i--) {
            System.out.println(this.id.stackTrace[i]);
        }
	
        System.out.println();
	for (String s : sqls) {
	    System.out.println(s);
	}
	
	System.out.println();
	System.out.println();
	System.out.println();
	
	
        StringBuffer sb = new StringBuffer();
        sb.append("digraph " + this.hashCode() + " { \n");

        printContext(sb);
        // Layout properties
//        sb.append("rankdir=RL;");
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
            for (Entry<StackTrace, Dependency> entry : query.getDependencies().entrySet()) {
                final Dependency dependency = entry.getValue();
                final Integer thisSequence = sequences.get(query.getId());
                final Integer dependencySequence = sequences.get(entry.getKey());
                
                sb.append(thisSequence + " -> " + dependencySequence);
                sb.append(" [label=\"");
                if (dependency.java != 0) {
                    sb.append("java: " + dependency.java + "/" + query.getExecutionCount() + "\\n");
                }
                if (dependency.raw != 0) {
                    sb.append("raw: " + dependency.raw + "/" + query.getExecutionCount() + "\\n");
                }
                if (dependency.war != 0) {
                    sb.append("war: " + dependency.war + "/" + query.getExecutionCount() + "\\n");
                }
                if (dependency.waw != 0) {
                    sb.append("waw: " + dependency.waw + "/" + query.getExecutionCount() + "\\n");
                }
                
                sb.append("\"];\n");
            }
        }
        
        final String[] colors = { "#7FC97F", "#BEAED4", "#FDC086", "#FFFF99", "#386CB0", "#F0027F" };
        
        // And also the execution order edges
        int orderIndex = 0;
        for (Entry<List<StackTrace>, Integer> e : executionOrders.entrySet()) {
            final List<StackTrace> list = e.getKey();
            if (e.getValue() < 700) continue;
            for (int i = 0; i < list.size() - 1; i++) {
                final StackTrace start = list.get(i);
                final StackTrace end = list.get(i+1);
                sb.append(sequences.get(start) + " -> " + sequences.get(end));
                sb.append(" [constraint=false,color=\""  + colors[orderIndex] + "\",label=\"" + (i+1) + "\"");
                if (i == 0) {
                    sb.append(",style=\"bold\"");
                }
                sb.append("];\n");
            }
            orderIndex++;
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
        sb.append("-------------------\\n");
        sb.append("Execution Time: " + query.getAvgExecutionTime() / 1e6 + "ms\\n");
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

    
//    private Map<StackTrace, Set<String>> sqlPerQuery = new HashMap<StackTrace, Set<String>>();
    private Set<String> sqls = new HashSet<String>();
    
    private void addSql(StackTrace stackTrace, String sql) {
//	Set<String> sqls = sqlPerQuery.get(sql);
//	if (sqls == null) {
//	    sqls = new HashSet<String>();
//	    sqlPerQuery.put(stackTrace, sqls);
//	}
	sqls.add(sql);
    }
    
    /**
     * Adds to this operation all executions that belong to a single
     * transaction.
     */
    public void addTransaction(Collection<Execution> collection) {
	// XXX Quick list of SQL statements
	for (Execution execution : collection) {
	    addSql(execution.getTrace(), execution.getSql());
	}
	
	
        // Organize all executions by id.
        Map<Long, Execution> executionsById = new HashMap<Long, Execution>();
        for (Execution execution : collection) {
            executionsById.put(execution.getId(), execution);
        }

        // Go over each execution, and update the query it belongs to
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

        // Now compute dependencies through the database.
        Map<String,Set<StackTrace>> updated = new HashMap<String, Set<StackTrace>>();
        Map<String,Set<StackTrace>> selected = new HashMap<String, Set<StackTrace>>();
        Map<StackTrace, Set<StackTrace>> rawDependencies = new HashMap<StackTrace, Set<StackTrace>>();
        Map<StackTrace, Set<StackTrace>> wawDependencies = new HashMap<StackTrace, Set<StackTrace>>();
        Map<StackTrace, Set<StackTrace>> warDependencies = new HashMap<StackTrace, Set<StackTrace>>();
        SortedMap<Integer, Execution> executionsByQueryId = new TreeMap<Integer, Execution>();
        
        for (Execution execution : collection) {
            // Sort the executions by queryId
            final int queryId = execution.getQueryId();
            executionsByQueryId.put(queryId, execution);
            if (queryId >= TransactionId.MAX_NUM_QUERIES) {
                System.err.println("Mmm, you seem to have more than " + TransactionId.MAX_NUM_QUERIES
                        + " queries per transaction. I don't think I can handle that many.");
                continue;
            }

            // Populate the updated and selected maps with empty sets for now,
            // to make the code that builds the dependencies more straightforward.
            for (String table : execution.getUpdatedTables()) {
                if (updated.get(table) == null) {
                    updated.put(table, new HashSet<StackTrace>());
                }
            }

            for (String table : execution.getSelectedTables()) {
                if (selected.get(table) == null) { 
                    selected.put(table, new HashSet<StackTrace>());
                }
            }
            
            // Fill the dependency maps with empty hash sets. Many of them might
            // not be used, but it makes the next part of the code much clearer
            if (rawDependencies.get(execution.getTrace()) == null) {
                rawDependencies.put(execution.getTrace(), new HashSet<StackTrace>());
            }
            if (wawDependencies.get(execution.getTrace()) == null) {
                wawDependencies.put(execution.getTrace(), new HashSet<StackTrace>());
            }
            if (warDependencies.get(execution.getTrace()) == null) {
                warDependencies.put(execution.getTrace(), new HashSet<StackTrace>());
            }

            
        }
        
        for (Entry<Integer, Execution> entry : executionsByQueryId.entrySet()) {
            Execution execution = entry.getValue();

            for (String table : execution.getSelectedTables()) {
                // Reads depend on all previous writes to the same table
                Set<StackTrace> writers = updated.get(table);
                if (writers != null) { // In case no one even writes to that table
                    for (StackTrace writer : writers) {
                        rawDependencies.get(execution.getTrace()).add(writer);
                    }
                }
            }

            for (String table : execution.getUpdatedTables()) {
                // Writes depend on all previous writes as a waw dependency
                Set<StackTrace> writers = updated.get(table);
                for (StackTrace writer : writers) {
                    wawDependencies.get(execution.getTrace()).add(writer);
                }
                // Writes also depend on all previous reads as a war dependency
                Set<StackTrace> readers = selected.get(table);
                if (readers != null) { // In case no one even reads that table
                    for (StackTrace reader : readers) {
                        warDependencies.get(execution.getTrace()).add(reader);
                    }
                }
            }
            
            // Update the read/deleted tables
            for (String table : execution.getUpdatedTables()) {
                updated.get(table).add(execution.getTrace());
            }

            for (String table : execution.getSelectedTables()) {
                selected.get(table).add(execution.getTrace());
            }
        }
        
        // Now take all the information and add it to the queries themselves
        for (Entry<StackTrace, Set<StackTrace>> entry : rawDependencies.entrySet()) {
            Query q = findQuery(entry.getKey());
            for (StackTrace s : entry.getValue()) {
                q.addRawDependency(findQuery(s));
            }
        }
        for (Entry<StackTrace, Set<StackTrace>> entry : warDependencies.entrySet()) {
            Query q = findQuery(entry.getKey());
            for (StackTrace s : entry.getValue()) {
                q.addWarDependency(findQuery(s));
            }
        }
        for (Entry<StackTrace, Set<StackTrace>> entry : wawDependencies.entrySet()) {
            Query q = findQuery(entry.getKey());
            for (StackTrace s : entry.getValue()) {
                q.addWawDependency(findQuery(s));
            }
        }
        
        // Finally compute the order in which the queries were executed.
        final List<StackTrace> executionOrder = new ArrayList<StackTrace>();
        for (Entry<Integer, Execution> entry : executionsByQueryId.entrySet()) {
            executionOrder.add(entry.getValue().getTrace());
        }
        
        this.addExecutionOrder(executionOrder);
    }

    /**
     * Adds to the count of possible execution orders the given execution order. 
     */
    private void addExecutionOrder(List<StackTrace> executionOrder) {
        Integer executionCount = executionOrders.get(executionOrder);
        if (executionCount == null) {
            executionCount = 0;
        }
        executionOrders.put(executionOrder, executionCount + 1);
        
    }
    
    @Override
    public String toString() {
        return id.toString();
    }

}
