package edu.stanford.pepe.newpostprocessing;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;

import edu.stanford.pepe.postprocessing.Execution;
import edu.stanford.pepe.runtime.StackTrace;
import edu.stanford.pepe.runtime.TransactionId;
import edu.stanford.pepe.runtime.lessmemory.IncompleteExecution;

public class BytecodeInstrumentationInterpreter {

    private Program p;

    public BytecodeInstrumentationInterpreter(Program p) {
        this.p = p;
    }

    public void load(String file) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
        try {
            @SuppressWarnings("unchecked")
            ConcurrentMap<StackTrace, List<IncompleteExecution>> executions = (ConcurrentMap<StackTrace, List<IncompleteExecution>>) ois
                    .readObject();
            processExecutions(executions);
        } finally {
            ois.close();
        }
    }

    private void processExecutions(ConcurrentMap<StackTrace, List<IncompleteExecution>> incompleteExecutions) {
        // First group the Executions by transactionId, creating full executions instead of incomplete ones.
        // IncompleteExecutions don't have the trace (to save memory). that's why this is required.
        final Map<Long, List<Execution>> executionsPerTransactionId = new HashMap<Long, List<Execution>>(
                incompleteExecutions.size() / 10); // guess around 10 ops/transaction
        for (Entry<StackTrace, List<IncompleteExecution>> entry : incompleteExecutions.entrySet()) {
            StackTrace trace = entry.getKey();
            for (IncompleteExecution execution : entry.getValue()) {
                final Execution e = new Execution(execution.getDependencies(), execution.getId(), trace,
                        execution.getElapsedTimeNanos(), execution.getSql());
                final long transactionId = e.getTransactionId();
                if (!executionsPerTransactionId.containsKey(transactionId)) {
                    executionsPerTransactionId.put(transactionId, new ArrayList<Execution>());
                }
                executionsPerTransactionId.get(transactionId).add(e);
            }
        }

        for (Entry<Long, List<Execution>> transactionEntry : executionsPerTransactionId.entrySet()) {
            long transactionId = transactionEntry.getKey();
            List<Execution> executions = transactionEntry.getValue();
            processTransaction(transactionId, executions);
        }
    }

    private void processTransaction(long transactionId, List<Execution> executions) {
        // Organize all executions by id.
        Map<Long, StackTrace> tracesById = new HashMap<Long, StackTrace>();
        for (Execution execution : executions) {
            tracesById.put(execution.getId(), cutTrace(execution));
        }

        // Now build the dependency map
        Map<StackTrace, Set<StackTrace>> dependenciesPerQuery = new HashMap<StackTrace, Set<StackTrace>>();

        // Go over each execution, and update the query it belongs to
        for (Execution execution : executions) {
            Set<StackTrace> dependencies = new HashSet<StackTrace>();
            StackTrace queryId = cutTrace(execution);
            dependenciesPerQuery.put(queryId, dependencies);

            for (long dependency : execution.getDependencies()) {
                // iterate over all dependencies, then over all dependencies of each, check if they belong to the same transaction
                long otherTid = TransactionId.getTransactionId(dependency);
                if (otherTid < execution.getTransactionId()) {
//                    System.out.println("Query depends on previous transaction");
                } else if (otherTid == execution.getTransactionId()) {
                    final StackTrace dependencyExecution = tracesById.get(dependency);
                    if (dependencyExecution == null) {
                        // XXX This is an error in the captured data, need to check if there's a bug.
                        return;
                    }
                    dependencies.add(dependencyExecution);
                } else {
                    // This means I depend on queries that started AFTER me. Whether intentional or not
                    // we're showing this to the user
//                    System.out.println("Query depends on future transaction");
                }
            }
        }

        p.addTransaction(dependenciesPerQuery, DependencyType.JAVA);

    }

    /**
     * Keeps only the part of the trace starting at the PreparedStatementHandle.
     */
    private StackTrace cutTrace(Execution execution) {
        final StackTraceElement[] trace = execution.getTrace().stackTrace;
        for (int i = 0; i < trace.length; i++) {
            StackTraceElement element = trace[i];
            if (element.getClassName().endsWith("PreparedStatementHandle")) {
                // Remove all traces before this element
                return new StackTrace(trace, i, trace.length);
            }
        }
        return new StackTrace(trace);
    }

}
