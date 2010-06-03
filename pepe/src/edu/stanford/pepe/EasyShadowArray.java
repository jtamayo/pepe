package edu.stanford.pepe;

import java.lang.reflect.Array;

/**
 * Simply a place to put a method that searches the current thread for the
 * shadow value of an array.
 * 
 * @author jtamayo
 */
public class EasyShadowArray {
	public static long[] getShadowArray(Object array) {
		MyThread t = MyThread.currentThread();
		if (t != null && t.__$$ARRAY_SHADOW_MAP != null) {
			return t.__$$ARRAY_SHADOW_MAP.getShadow(array);
		} else {
			// When the thread is null, simply create a new array
			return new long[Array.getLength(array)];
		}
	}
	
}


class MyThread extends Thread{
	
	public MyThread() {
		this.__$$ARRAY_SHADOW_MAP = new ArrayShadowMap();
	}
	
	public static MyThread currentThread() {
		return null;
	}
	ArrayShadowMap __$$ARRAY_SHADOW_MAP;
}