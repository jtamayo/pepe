package edu.stanford.pepe.runtime;

import java.util.Arrays;

/**
 * Represents a Stack trace as obtained from {@link Throwable#getStackTrace()}
 * 
 * @author jtamayo
 */
public class StackTrace {
	public final StackTraceElement[] stackTrace;

	public StackTrace(StackTraceElement[] stackTrace) {
		this.stackTrace = stackTrace.clone();
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
			if (!other.stackTrace[i].equals(this.stackTrace[i])) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hashcode = 0;
		for (StackTraceElement element : stackTrace) {
			hashcode += 31 * element.hashCode();
		}
		return hashcode;
	}
	
	@Override
	public String toString() {
		return Arrays.toString(stackTrace);
	}
}