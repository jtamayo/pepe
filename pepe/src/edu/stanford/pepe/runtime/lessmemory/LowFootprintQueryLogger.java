package edu.stanford.pepe.runtime.lessmemory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import edu.stanford.pepe.runtime.StackTrace;

public class LowFootprintQueryLogger {

	// Not thread-safe, how do I fix it.
	private static ConcurrentMap<StackTrace, List<IncompleteExecution>> executionsPerQuery = new ConcurrentHashMap<StackTrace, List<IncompleteExecution>>();

	public static synchronized void log(String sql, Set<Long> dependencies, long taint) {
		final StackTrace trace = new StackTrace(new Throwable().getStackTrace());
		List<IncompleteExecution> executions = executionsPerQuery.get(trace);
		if (executions == null) {
			// TODO: Code review
			// No matter which thread creates the list, there's still a single one at the end
			executionsPerQuery.putIfAbsent(trace, Collections.synchronizedList(new ArrayList<IncompleteExecution>()));
			executions = executionsPerQuery.get(trace);
			System.out.println("Queries: " + executionsPerQuery.size());
		}

		executions.add(new IncompleteExecution(dependencies, taint));
	}

	public static void toDisk() {
		File f = new File(System.currentTimeMillis() + ".dmp");
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
			try {
				oos.writeObject(executionsPerQuery);
			} finally {
				oos.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
