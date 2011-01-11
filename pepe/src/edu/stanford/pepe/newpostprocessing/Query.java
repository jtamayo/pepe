package edu.stanford.pepe.newpostprocessing;

import java.util.HashMap;
import java.util.Map;

import edu.stanford.pepe.postprocessing.Execution;
import edu.stanford.pepe.runtime.StackTrace;

/**
 * A query represents a specific call point in the code, as defined by the stack
 * trace of the point where the SQL query is invoked.
 * 
 * @author jtamayo
 */
public class Query {
    
    /**
     * Dependencies of this query to other queries in the same Operation.
     */
    private final Map<StackTrace, Dependency> dependencies = new HashMap<StackTrace, Dependency>();
    
    /**
     * Represents all the ways in which a Query might depend on another query.
     */
    public static class Dependency {
        /** Dependency tracked through the java code */
        public int java;
        /** Forward dependency tracked through JDT */
        public int dbDependency;
        /** Antidependency discovered through JDT */
        public int dbAntidependency;
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
    
    /**
     * Identifier for this query.
     */
	private final StackTrace id;
	
	/** Number of times the query was executed */
	private int executionCount = 0;
	private long totalTimeNanos;
	
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
}
