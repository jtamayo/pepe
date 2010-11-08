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
    
    /*
     * Ok, so I need an explicit representation of the dependency between two queries. Hmm, this is going to be painfully heavy. 
     * In some sense I already have such dependency, it's just I'm not managin iit properly. What I need is a single map from 
     * dependent query to a structure that keeps track of all the types of dependencies. 
     */
    private final Map<StackTrace, Dependency> dependencies = new HashMap<StackTrace, Dependency>();
    
    /**
     * Represents all the ways in which a Query might depend on another query.
     */
    public static class Dependency {
        /** Dependency tracked through the java code */
        public int java;
        public int raw;
        public int waw;
        public int war;
    }
    
    private Dependency dependencyFor(Query q) {
        Dependency d = dependencies.get(q.getId());
        if (d == null) {
            d = new Dependency();
            dependencies.put(q.getId(), d);
        }
        return d;
    }
    
	private final StackTrace id;
	
	/** Number of times the query was executed */
	private int executionCount = 0;
	private long totalTimeNanos;
	/** Tables selected in this query */
	private Map<String, Integer> selectedTables = new HashMap<String, Integer>();
	private Map<String, Integer> updatedTables = new HashMap<String, Integer>();
	
	public StackTrace getId() {
		return id;
	}

	public Query(StackTrace stackTrace) {
		this.id = new StackTrace(stackTrace);
	}

	public Map<StackTrace, Dependency> getDependencies() {
        return dependencies;
    }

	public void addDependency(Query q) {
	    dependencyFor(q).java++;
	}
	
	public void addRawDependency(Query q) {
	    dependencyFor(q).raw++;
    }

	public void addWarDependency(Query q) {
	    dependencyFor(q).war++;
	}
		
	public void addWawDependency(Query q) {
	    dependencyFor(q).waw++;
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
