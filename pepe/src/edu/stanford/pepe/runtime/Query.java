package edu.stanford.pepe.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.stanford.pepe.postprocessing.Execution;

/**
 * A query represents a specific call point in the code, as defined by the stack
 * trace of the point where the SQL query is invoked.
 * 
 * @author jtamayo
 */
public class Query {
	private final StackTrace id;
	private final Map<StackTrace, Integer> dependencyValues = new HashMap<StackTrace, Integer>();
	/** Number of times the query was executed */
	private int executionCount = 0;
	private long totalTimeNanos;
	/** Tables selected in this query */
	private Map<String, Integer> selectedTables = new HashMap<String, Integer>();
	private Map<String, Integer> updatedTables = new HashMap<String, Integer>();
	
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

	/**
	 * Number of times the query was executed.
	 */
	public int getExecutionCount() {
		return executionCount;
	}
	
	public double getAvgExecutionTime() {
		return totalTimeNanos / executionCount;
	}
	
	/**
	 * Increments by one the execution count.
	 */
	public void addExecution(Execution execution) {
		executionCount++;
		totalTimeNanos += execution.getElapsedTimeNanos();
		for (String table : execution.getSelectedTables()) {
            incrementSelectedTable(table);
        }
		
		for (String table : execution.getUpdatedTables()) {
            incrementUpdatedTable(table);
        }
	}

    private void incrementUpdatedTable(String table) {
        Integer count = updatedTables.get(table);
        if (count == null) {
            count = 0;
        }
        updatedTables.put(table, count + 1);
    }

    private void incrementSelectedTable(String table) {
        Integer count = selectedTables.get(table);
        if (count == null) {
            count = 0;
        }
        selectedTables.put(table, count + 1);
    }

    public Map<String, Integer> getUpdatedTables() {
        return updatedTables;
    }
    
    public Map<String, Integer> getSelectedTables() {
        return selectedTables;
    }
    
}
