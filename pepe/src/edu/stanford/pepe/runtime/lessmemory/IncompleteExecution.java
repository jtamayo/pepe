/**
 * 
 */
package edu.stanford.pepe.runtime.lessmemory;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

public class IncompleteExecution implements Serializable {
	private static final long serialVersionUID = 1343620557464331497L;
	
	private final long[] dependencies;
	private final long id;
	
	public IncompleteExecution(Set<Long> dependencies, long id) {
		this.dependencies = new long[dependencies.size()];
		
		int i = 0;
		for (Iterator<Long> it = dependencies.iterator(); it.hasNext();) {
			this.dependencies[i] = it.next();
			i++;
		}
		
		this.id = id;
	}
	
	public long[] getDependencies() {
		return dependencies;
	}
	
	public long getId() {
		return id;
	}
}