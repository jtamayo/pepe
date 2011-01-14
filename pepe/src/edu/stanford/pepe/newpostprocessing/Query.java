package edu.stanford.pepe.newpostprocessing;

import java.util.HashMap;
import java.util.Map;

import edu.stanford.pepe.runtime.StackTrace;

/**
 * A query represents a specific call point in the code, as defined by the stack
 * trace of the point where the SQL query is invoked.
 * 
 * @author jtamayo
 */
public class Query {
    
    public static class DependencyCount {
        private final DependencyType type;
        private int count;
        
        public DependencyCount(DependencyType type) {
            this.type = type;
            count = 0;
        }
        
        public void addCount()  {
            count++;
        }
        
        public int getCount() {
            return count;
        }
        
        public DependencyType getType() {
            return type;
        }
    }
    
    /**
     * Dependencies of this query to other queries in the same Operation.
     */
    private final Map<StackTrace, DependencyCount> dependencies = new HashMap<StackTrace, Query.DependencyCount>();
    
    private DependencyCount countFor(Query q, DependencyType type) {
        DependencyCount d = dependencies.get(q.getId());
        if (d == null) {
            d = new DependencyCount(type);
            dependencies.put(q.getId(), d);
        }
        return d;
    }
    
    /**
     * Identifier for this query.
     */
	private final StackTrace id;
	
	public StackTrace getId() {
		return id;
	}

	public Query(StackTrace stackTrace) {
		this.id = new StackTrace(stackTrace);
	}

	public Map<StackTrace, DependencyCount> getDependencies() {
        return dependencies;
    }

	public void addDependency(Query q, DependencyType type) {
	    countFor(q, type).addCount();
	}
	
    public int getExecutionCount() {
        return 1;
    }    
}
