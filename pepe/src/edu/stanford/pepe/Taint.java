package edu.stanford.pepe;

import edu.stanford.pepe.org.objectweb.asm.Type;

public class Taint {
	private static final Type TAINT_TYPE = Type.getType(Taint.class);
	public static final String TAINT_INTERNAL_NAME = TAINT_TYPE.getInternalName();
	public static final String MEET_OP_NAME = "meet";
	public static String MEET_OP_DESCRIPTOR = "(JJ)J"; // TODO: Make this less brittle by somehow linking it to the real method

	public static long meet(long a, long b) {
		return 0;
	}
	
}
