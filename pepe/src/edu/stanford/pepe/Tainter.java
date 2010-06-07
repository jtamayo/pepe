package edu.stanford.pepe;

import java.lang.reflect.Field;

/**
 * A simple class whose return values are always tainted by the taint passed in
 * the constructor. This class should not be instrumented.
 * 
 * @author jtamayo
 */
public class Tainter {
	private final long taint;

	public Tainter(long taint) {
		this.taint = taint;
	}
	
	public int getInt(int i) {
		Class<Thread> c = Thread.class;
		try {
			Field f = c.getField(ThreadInstrumenter.RETURN_VALUE_NAME);
			f.set(Thread.currentThread(), taint);
			return i;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
