package edu.stanford.pepe;

import java.lang.reflect.Field;

/**
 * Simple convenience methods for determining if a given value is tainted. It
 * should not be instrumented.
 * 
 * @author jtamayo
 */
public class TaintCheck {

	/**
	 * Returns the taint associated with the given parameter.
	 * @param i
	 * @return
	 */
	public static long getTaint(int i) {
		return 0;
//		try {
//			Field f = Thread.class.getField(ThreadInstrumenter.PARAMETER_FIELD_PREFIX + 0);
//			Thread t = Thread.currentThread();
//			System.out.println(t);
//			return f.getLong(t);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
	}
}
