package edu.stanford.pepe.runtime;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

/**
 * Represents a Stack trace as obtained from {@link Throwable#getStackTrace()}
 * 
 * @author jtamayo
 */
public class StackTrace implements Serializable {
    private static final long serialVersionUID = -4643494257012911255L;

    public final StackTraceElement[] stackTrace;

    public StackTrace(StackTraceElement[] stackTrace) {
        this.stackTrace = stackTrace.clone();
    }

    public StackTrace(StackTrace other) {
        this(other.stackTrace);
    }

    /**
     * Creates a new StackTrace by taking only a part of the given
     * StackTraceElement[] array.
     * 
     * @param e
     *            source for the trace
     * @param begin
     *            start index from which data will be copied (inclusive)
     * @param end
     *            one past the index of the last element to copy from
     */
    public StackTrace(StackTraceElement[] e, int begin, int end) {
        this.stackTrace = new StackTraceElement[end - begin];
        System.arraycopy(e, begin, this.stackTrace, 0, end - begin);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof StackTrace)) {
            return false;
        }
        StackTrace other = (StackTrace) obj;
        if (other.stackTrace.length != this.stackTrace.length) {
            return false;
        }
        for (int i = 0; i < stackTrace.length; i++) {
            if (!equal(other.stackTrace[i], this.stackTrace[i])) {
                return false;
            }
        }
        return true;
    }

    public static boolean equal(StackTraceElement a, StackTraceElement b) {
        if (a == b)
            return true;
        return a.getClassName().equals(b.getClassName()) && eq(a.getMethodName(), b.getMethodName())
                && eq(a.getFileName(), b.getFileName());
    }

    private static boolean eq(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }
    
    private static int noLineNumberHashcode(StackTraceElement e) {
        int result = 31*e.getClassName().hashCode() + e.getMethodName().hashCode();
        result = 31*result + (e.getFileName() == null ?   0 : e.getFileName().hashCode());
        return result;
    
    }

    @Override
    public int hashCode() {
        int hashcode = 0;
        for (StackTraceElement element : stackTrace) {
            hashcode += 31 * noLineNumberHashcode(element);
        }
        return hashcode;
    }

    @Override
    public String toString() {
        return Arrays.toString(stackTrace);
    }
    
    /**
     * Creates a new StackTrace that is the longest possible suffix of all
     * queries in the given Collection.
     */
    public static StackTrace getSuffix(Collection<StackTrace> c) {
        final StackTrace prototype = c.iterator().next(); // get a query, all of the rest will be compared to this one
        for (int i = 0; i < prototype.stackTrace.length; i++) {
            for (StackTrace query : c) {
                if (!equal(prototype.stackTrace[prototype.stackTrace.length - i - 1],
                        query.stackTrace[query.stackTrace.length - i - 1])) {
                    StackTraceElement[] suffix = new StackTraceElement[i];
                    System.arraycopy(prototype.stackTrace, prototype.stackTrace.length - i, suffix, 0, i);
                    return new StackTrace(suffix);
                }
            }
        }
        return new StackTrace(new StackTraceElement[] {}); // There is only one query
    }
}