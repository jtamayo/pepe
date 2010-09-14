package edu.stanford.pepe.runtime;

import java.util.HashMap;
import java.util.Map;

/**
 * A query represents a specific call point in the code, as defined by the stack
 * trace of the point where the SQL query is invoked.
 * 
 * @author jtamayo
 */
public class Query {
	private final StackTrace id;
	private final Map<StackTrace, Integer> dependencyValues = new HashMap<StackTrace, Integer>();
	
	public StackTrace getId() {
		return id;
	}

	public Map<StackTrace, Integer> getDependencyValues() {
		return dependencyValues;
	}


	public Query(StackTrace stackTrace) {
		this.id = new StackTrace(stackTrace);
	}

	public void addDependency(Query findQuery) {
		Integer oldValue = dependencyValues.get(findQuery.id);
		if (oldValue == null) {
			oldValue = 0;
		}
		dependencyValues.put(findQuery.id, oldValue + 1);
	}
	
	/**
	 * Represents the dependency of a query to a previous transaction. 
	 */
	public static class PreviousTransactionQuery extends Query {

		private static final PreviousTransactionQuery instance = new PreviousTransactionQuery();
		
		public static PreviousTransactionQuery get() {
			return instance;
		}
		
		private PreviousTransactionQuery() {
			super(new StackTrace(new StackTraceElement[]{}));
		}
		
	}
}
