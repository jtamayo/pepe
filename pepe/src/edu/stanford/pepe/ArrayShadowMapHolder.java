package edu.stanford.pepe;

import java.lang.reflect.Array;

public class ArrayShadowMapHolder {
	
	private static ThreadLocal<ArrayShadowMap> map = new ThreadLocal<ArrayShadowMap>() {
		protected ArrayShadowMap initialValue() {
			return new ArrayShadowMap();
		};
	};
	
	public static long[] getArrayShadow(Object array) {
		if (Thread.currentThread() != null && map != null && map.get() != null) {
			long[] shadow = map.get().getShadow(array);
			return shadow != null ? shadow : new long[Array.getLength(array)]; 
		} else {
			return new long[Array.getLength(array)];
		}
	}
	
}
