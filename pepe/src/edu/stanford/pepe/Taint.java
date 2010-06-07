package edu.stanford.pepe;

import edu.stanford.pepe.org.objectweb.asm.Type;

public class Taint {
	private static final Type TAINT_TYPE = Type.getType(Taint.class);
	public static final String TAINT_INTERNAL_NAME = TAINT_TYPE.getInternalName();
	public static final String MEET_OP_NAME = "meet";
	public static String MEET_OP_DESCRIPTOR = "(JJ)J";

	public static long meet(long a, long b) {
		int dif = (int) a - (int) b;
		if (dif == 0) {
			return a | b;
		} else if (dif < 0) {
			return b;
		} else {
			return a;
		}
	}
	
}
