package edu.stanford.pepe.newpostprocessing;

import java.util.Collection;
import java.util.HashMap;
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

    private final Map<StackTrace, Operation> operations = new HashMap<StackTrace, Operation>();
    
    /**
     * Processes a transaction executed in the program.
     * 
     * @param dependencies
     *            A map from queries (identified by their stack trace) to their
     *            dependencies (also identified by ther stack trace).
     */
    public void addTransaction(Map<StackTrace, Set<StackTrace>> dependencies, DependencyType type) {
        final Set<StackTrace> queries = dependencies.keySet();
        final StackTrace operationSuffix = StackTrace.getSuffix(queries);
        final Operation o = findOperation(operationSuffix);
        o.addTransaction(dependencies, type);
    }

    private Operation findOperation(StackTrace operationSuffix) {
        Operation o = operations.get(operationSuffix);
        if (o == null) {
            o = new Operation(operationSuffix);
            operations.put(operationSuffix, o);
            System.out.println("|||" + operationSuffix);
        }
        return o;
    }

    public Collection<Operation> getOperations() {
        return operations.values();
    }

}
