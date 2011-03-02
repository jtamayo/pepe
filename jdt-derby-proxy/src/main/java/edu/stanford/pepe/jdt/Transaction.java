package edu.stanford.pepe.jdt;

import java.io.Serializable;
import java.util.List;

public class Transaction implements Serializable {

    private static final long serialVersionUID = -3142849059532176569L;
    private final long transactionId;
    private final List<StackTraceElement[]> queries;
    private final Fingerprint[][] fingerprints;
    private final List<Boolean> isWrite;

    public Transaction(long transactionId, List<StackTraceElement[]> queries, List<Boolean> isWrite,
            Fingerprint[][] fingerprints) {
        this.transactionId = transactionId;
        this.queries = queries;
        this.isWrite = isWrite;
        this.fingerprints = fingerprints;
        checkConsistency();
    }

    private void checkConsistency() {
        // Compute the list of reads and writes.
        if (isWrite.size() != queries.size()) {
            throw new IllegalArgumentException("queries.size() and isWrite.size() should be equal");
        }

        int reads = 0;
        int writes = 0;

        for (int i = 0; i < queries.size(); i++) {
            if (isWrite.get(i)) {
                writes++;
            } else {
                reads++;
            }
        }

        // Check that the number of slots matches
        if (fingerprints.length != writes + 1) {
            throw new IllegalArgumentException("Wrong number of slots. First dimension of fingerprints is "
                    + fingerprints.length + " but should be " + (writes + 1));
        }

        for (int i = 0; i < fingerprints.length; i++) {
            Fingerprint[] slot = fingerprints[i];
            if (slot.length != reads) {
                throw new IllegalArgumentException("Slot" + i + " has size " + slot.length + " but should have size "
                        + reads);
            }
        }
    }

    public long getTransactionId() {
        return transactionId;
    }

    public List<StackTraceElement[]> getQueries() {
        return queries;
    }

    /**
     * First dimension is the slot, second dim is the index of the SQL Read.
     * There is always at least one slot, even when there are no writes.
     */
    public Fingerprint[][] getFingerprints() {
        return fingerprints;
    }

    public List<Boolean> getIsWrite() {
        return isWrite;
    }

}
