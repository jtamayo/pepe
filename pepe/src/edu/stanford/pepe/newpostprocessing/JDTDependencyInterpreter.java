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

import edu.stanford.pepe.jdt.Fingerprint;
import edu.stanford.pepe.jdt.Transaction;
import edu.stanford.pepe.runtime.StackTrace;

public class JDTDependencyInterpreter {

    private final Program p;

    public JDTDependencyInterpreter(Program p) {
        this.p = p;

    }

    public void load(String file) throws IOException, ClassNotFoundException {

        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
        try {
            @SuppressWarnings("unchecked")
            List<Transaction> executions = (List<Transaction>) ois.readObject();
            processTransactions(executions);
        } finally {
            ois.close();
        }

    }

    private void processTransactions(List<Transaction> executions) {
        int i = 0;
        for (Transaction t : executions) {
            processTransaction(t);
            i++;
        }
    }

    private void processTransaction(Transaction t) {
        List<StackTrace> reads = new ArrayList<StackTrace>();
        List<Integer> readSlots = new ArrayList<Integer>();
        List<StackTrace> writes = new ArrayList<StackTrace>();
        Map<StackTrace, Set<StackTrace>> dependencies = new HashMap<StackTrace, Set<StackTrace>>();

        int slot = 0;
        for (int i = 0; i < t.getQueries().size(); i++) {
            StackTraceElement[] e = t.getQueries().get(i);
            final StackTrace stackTrace = cutTrace(e);// new StackTrace(e);
            dependencies.put(stackTrace, new HashSet<StackTrace>());
            if (t.getIsWrite().get(i)) {
                writes.add(stackTrace);
                slot++;
            } else {
                reads.add(stackTrace);
                readSlots.add(slot);
            }
        }

        computeWriteDependencies(writes);

        if (!reads.isEmpty()) {
            for (int i = 0; i < reads.size(); i++) {
                final StackTrace read = reads.get(i);
                final Fingerprint canonical = t.getFingerprints()[readSlots.get(i)][i];
                for (int s = readSlots.get(i); s >= 0; s--) {
                    final Fingerprint other = t.getFingerprints()[s][i];
                    if (!other.equals(canonical)) {
                        // This is the different slot, create a dependency between the read and the write that follows the slot
                        dependencies.get(read).add(writes.get(s));
                    }
                }
            }

            p.addTransaction(dependencies, DependencyType.DB_DEPENDENCY);
        }

    }

    /**
     * Returns a new StackTrace based on the given StackTraceElement[], ignoring
     * all elements after the first PreparedStatementHandler.
     * 
     */
    private StackTrace cutTrace(StackTraceElement[] trace) {
        for (int i = 0; i < trace.length; i++) {
            StackTraceElement element = trace[i];
            if (element.getClassName().endsWith("PreparedStatementHandle")) {
                // Remove all traces before this element
                return new StackTrace(trace, i, trace.length);
            }
        }
        return new StackTrace(trace);
    }

    /**
     * Creates a dependency from one write to the next, because we assume writes
     * cannot be reordered.
     */
    private void computeWriteDependencies(List<StackTrace> writes) {
        //        throw new RuntimeException("Not yet implemented");
    }

}
