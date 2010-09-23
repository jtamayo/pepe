package edu.stanford.pepe.postprocessing;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import edu.stanford.pepe.runtime.StackTrace;
import edu.stanford.pepe.runtime.TransactionId;

public class Execution {
	private final Set<Long> dependencies;
	private final long id;
	private final StackTrace trace;
	private final long elapsedTimeNanos;

	/**
	 * Cache of StackTraces to reduce memory consumption. The StackTraces are
	 * essentially immutable, hence they can be reused;
	 */
	private static final ConcurrentMap<StackTrace, StackTrace> stackTraceCache = new ConcurrentHashMap<StackTrace, StackTrace>();

	/**
	 * Looks for the given trace in the cache. If not found, creates a new entry
	 * and returns it.
	 */
	private static StackTrace cache(StackTrace trace) {
		StackTrace output = stackTraceCache.get(trace);
		if (output == null) {
			final StackTrace newKey = new StackTrace(trace.stackTrace);
			stackTraceCache.putIfAbsent(newKey, newKey);
			output = stackTraceCache.get(newKey);
		}
		return output;
	}

	/**
	 * Builds a new Execution. Uses a cached copy of the given StackTrace to
	 * reduce memory consumption.
	 */
	public Execution(long[] dependencies, long id, StackTrace trace, long elapsedTimeNanos) {
		this.elapsedTimeNanos = elapsedTimeNanos;
		this.id = id;
		this.trace = cache(trace);

		this.dependencies = new HashSet<Long>();

		for (long dependency : dependencies) {
			List<Long> dependencyIds = TransactionId.getDependencies(dependency);
			this.dependencies.addAll(dependencyIds);
		}
	}

	/**
	 * Returns the set of Executions on which this Execution depends. 
	 */
	public Set<Long> getDependencies() {
		return dependencies;
	}

	public long getId() {
		return id;
	}

	public StackTrace getTrace() {
		return trace;
	}
	
	public long getTransactionId() {
		return TransactionId.getTransactionId(id);
	}
	
	public int getQueryId() {
		return TransactionId.getQueryId(id);
	}

	public long getElapsedTimeNanos() {
		return elapsedTimeNanos;
	}
	

}
