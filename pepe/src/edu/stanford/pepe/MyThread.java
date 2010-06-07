package edu.stanford.pepe;

public class MyThread {
	public long __$$RETURN_VAL;
	
	public static void setReturnVal(long l) {
		MyThread t = currentThread();
		if (t != null) {
			t.__$$RETURN_VAL = l;
		}
	}
	
	public static MyThread currentThread() {
		return null;
	}
}
