package edu.stanford.pepe.newpostprocessing;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import edu.stanford.pepe.runtime.StackTrace;

/**
 * Represents many executions of a program. It keeps track of all the operations
 * and queries executed, and outputs graphs for each.
 * 
 * @author jtamayo
 */
public class Program {

    /**
     * Processes a transaction executed in the program.
     * 
     * @param dependencies
     *            A map from queries (identified by their stack trace) to their
     *            dependencies (also identified by ther stack trace).
     */
    public void addTransaction(Map<StackTrace, Set<StackTrace>> dependencies, DependencyType type) {
        final Set<StackTrace> queries = dependencies.keySet();
        final StackTrace operationSuffix = getSuffix(queries);
        /*
         * Hmm, so, the detail is that I may have several types of dependencies, and only one of them is
         * what I need for this invocation. How should I keep track of multiple sets of dependencies?
         * 
         * Again, options are:
         * Keep them in each query
         * Keep them outside, in a different map. 
         * 
         * The trick is, there's several types of dependencies, and each of them has certain consequences. It 
         * may be better to simply do what I'm doing now: create a Query object that knows its dependencies, and
         * has different types of dependencies. That way, it's quite trivial to add all the dependencies.
         * 
         * Now, to add a new type of dependency, I'd only need to add it to the DependencyType object, and
         * to the Dependency inner class in Query. If I don't do it this way, Id' have to keep a crapload of maps,
         * one for each "graph", and that sounds kind of annoying. Plus, keeping them in a single object allows
         * me to plot several details at once. Ok, so the approach I have now is probably the best. Let's just stay with it.
         */
    }

    private static StackTrace getSuffix(Collection<StackTrace> c) {
        final StackTrace prototype = c.iterator().next(); // get a query, all of the rest will be compared to this one
        for (int i = 0; i < prototype.stackTrace.length; i++) {
            for (StackTrace query : c) {
                if (!prototype.stackTrace[prototype.stackTrace.length - i - 1]
                        .equals(query.stackTrace[query.stackTrace.length - i - 1])) {
                    // i is the length of the suffix
                    StackTraceElement[] suffix = new StackTraceElement[i];
                    System.arraycopy(prototype.stackTrace, prototype.stackTrace.length - i, suffix, 0, i);
                    return new StackTrace(suffix);
                }
            }
        }
        return new StackTrace(new StackTraceElement[] {}); // There is only one query
    }

}
