package edu.stanford.pepe;

/**
 * Simple convenience methods for determining if a given value is tainted. All
 * of its methods are constructed at runtime by {@link TaintCheckInstrumenter}.
 * 
 * @author jtamayo
 */
public class TaintCheck {

	/**
	 * Returns the taint associated with the given parameter.
	 * 
	 * @param i
	 * @return
	 */
	public static long getTaint(int i) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the taint associated with the given parameter.
	 * 
	 * @param o
	 * @return
	 */
	public static long getTaint(Object o) {
		throw new UnsupportedOperationException();
	}

	/**
	 * At runtime this method is modified to taint the return value with the
	 * given taint.
	 * 
	 * @param taint
	 * @return
	 */
	public static int taint(int retVal, long taint) {
		throw new UnsupportedOperationException();
	}

	/**
	 * At runtime this method is modified to taint the return value with the
	 * given taint.
	 * 
	 * @param ref
	 *            the reference to taint
	 * @param taint
	 *            the taint to introduce
	 * @return the tainted reference
	 */
	public static <T> T taint(T ref, long taint) {
		throw new UnsupportedOperationException();
	}
}
