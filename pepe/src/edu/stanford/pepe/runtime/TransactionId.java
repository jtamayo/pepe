package edu.stanford.pepe.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple class for generating transactionIds and queryIds.
 * 
 * @author jtamayo
 */
public class TransactionId {
	
	private static ThreadLocal<TransactionId> threadTransactionId = new ThreadLocal<TransactionId>() {
		@Override
		protected TransactionId initialValue() {
			return newTransaction();
		}
	};

	private static final long MAX_TRANSACTION_ID = 0xFFFFFFFFl;

	public static final int MAX_NUM_QUERIES = 31;

	// Start at 0, so that the first transaction is 1
	private static final AtomicLong nextTransactionId = new AtomicLong(0);
	private final long transactionId;
	private final AtomicLong nextQueryId;

	private TransactionId(long transactionId) {
		this.transactionId = transactionId;
		// start at -1, so that the first query is numbered 0
		this.nextQueryId = new AtomicLong(-1);
	}

	/**
	 * Obtains a new transaction identifier, ready to generate taint for query
	 * 0.
	 */
	public static TransactionId newTransaction() {
		return new TransactionId(Math.min(nextTransactionId.incrementAndGet(), MAX_TRANSACTION_ID));
	}
	
	public static TransactionId getCurrentTransaction() {
		return threadTransactionId.get();
	}

	/**
	 * To be invoked after a JDBC Connection starts a new transaction, either because of a commit
	 * or a rollback.
	 */
	public static void onNewTransaction() {
	    System.out.println("New transaction");
		threadTransactionId.set(newTransaction());
	}

	/**
	 * Gets the taint for the next query in this transaction. It will have the
	 * transactionId of this transaction an one of most significant 32 bits set.
	 */
	public long getNextTaint() {
		nextQueryId.incrementAndGet();
		// Saturate at MAX_NUM_QUERIES
		final long queryId = nextQueryId.longValue() < MAX_NUM_QUERIES ? nextQueryId.longValue() : MAX_NUM_QUERIES;
		final long taint = buildTaint((int) queryId, transactionId);
		return taint;
	}

	private static long buildTaint(final int queryId, final long transactionId) {
		return (1l << (32 + queryId)) | transactionId;
	}

	/**
	 * Returns the transactionId for a given taint. The transactionId are the 32
	 * least significant bits.
	 */
	public static long getTransactionId(long taint) {
		return taint & MAX_TRANSACTION_ID;
	}

	/**
	 * Returns true if the given taint depends on query queryId. It simply
	 * checks if bit (32+queryId) is set.
	 */
	public static boolean isDependentOnQuery(long taint, int queryId) {
		if (queryId > MAX_NUM_QUERIES) {
			throw new IllegalArgumentException("There cannot be more than " + MAX_NUM_QUERIES + "queries");
		}
		final long expectedTaint = 1l << (32 + queryId);
		return (expectedTaint & taint) != 0;
	}

	/**
	 * Returns the smallest integer for which
	 * {@link #isDependentOnQuery(long, int)} returns true.
	 * 
	 * @throws IllegalArgumentException
	 *             when the given taint does not depend on any query
	 */
	public static final int getQueryId(long taint) throws IllegalArgumentException {
		for (int i = 0; i <= MAX_NUM_QUERIES; i++) {
			if (isDependentOnQuery(taint, i))
				return i;
		}
		throw new IllegalArgumentException("Taint " + Long.toHexString(taint) + " does not depend on any query");
	}

	/**
	 * Returns an array of queries on which the given taint depends. The
	 * dependencies share the same transactionId as the given taint, and they
	 * have only one of the query bits set.
	 */
	public static final List<Long> getDependencies(long taint) {
		ArrayList<Long> dependencies = new ArrayList<Long>();
		for (int i = 0; i < 32; i++) {
			if (isDependentOnQuery(taint, i)) {
				dependencies.add(buildTaint(i, getTransactionId(taint)));
			}
		}
		return dependencies;
	}

}
