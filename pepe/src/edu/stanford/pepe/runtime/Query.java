package edu.stanford.pepe.runtime;

import java.util.HashMap;
import java.util.Map;

import edu.stanford.pepe.postprocessing.Execution;

/**
 * A query represents a specific call point in the code, as defined by the stack
 * trace of the point where the SQL query is invoked.
 * 
 * @author jtamayo
 */
public class Query {
    
    private static class DependencyMap {
        private final Map<StackTrace, Integer> values = new HashMap<StackTrace, Integer>();
        
        public void addDependency(Query findQuery) {
            Integer oldValue = getValues().get(findQuery.id);
            if (oldValue == null) {
                oldValue = 0;
            }
            getValues().put(findQuery.id, oldValue + 1);
        }

        public Map<StackTrace, Integer> getValues() {
            return values;
        }
    }
    
	private final StackTrace id;
	/** Dependencies tracked through java data flow */
	private final DependencyMap javaDependencies = new DependencyMap();
	
	private final DependencyMap rawDependencies = new DependencyMap();
	private final DependencyMap warDependencies = new DependencyMap();
	private final DependencyMap wawDependencies = new DependencyMap();
	
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
		return javaDependencies.getValues();
	}

	public Query(StackTrace stackTrace) {
		this.id = new StackTrace(stackTrace);
	}

	public void addDependency(Query q) {
	    javaDependencies.addDependency(q);
	}
	
	public void addRawDependency(Query q) {
	    rawDependencies.addDependency(q);
    }
	
	public Map<StackTrace, Integer> getRawDependencies() {
	    return rawDependencies.getValues();
	}

	public void addWarDependency(Query q) {
	    warDependencies.addDependency(q);
	}
	
	public Map<StackTrace, Integer> getWarDependencies()  {
	    return warDependencies.getValues();
	}
	
	public void addWawDependency(Query q) {
	    wawDependencies.addDependency(q);
	}
	
	public Map<StackTrace, Integer> getWawDependencies() {
        return wawDependencies.getValues();
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
