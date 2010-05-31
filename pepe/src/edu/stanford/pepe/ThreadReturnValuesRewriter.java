package edu.stanford.pepe;

import edu.stanford.pepe.org.objectweb.asm.Opcodes;
import edu.stanford.pepe.org.objectweb.asm.tree.ClassNode;
import edu.stanford.pepe.org.objectweb.asm.tree.FieldNode;

public class ThreadReturnValuesRewriter implements Opcodes {
	
	public static final int PARAMETER_NUMBER = 30;
	public static final String PARAMETER_FIELD_PREFIX = "__$$PARAMETER";
	public static final String RETURN_VALUE_NAME = "__$$RETURN_VAL";
	public static final String GET_RETURN_VALUE = "get" + RETURN_VALUE_NAME;
	
	/**
	 * Add the return value and parameter fields to java.lang.Thread.
	 * @param cn
	 */
	public static void rewrite(ClassNode cn) {
		if (cn.name.equals("java/lang/Thread")) {
			for (int i = 0; i < PARAMETER_NUMBER; i++) {
				int newAccess = ACC_PUBLIC;
				FieldNode fn = new FieldNode(newAccess, PARAMETER_FIELD_PREFIX + i, ShadowFieldRewriter.TAINT_TYPE.getDescriptor(), null, new Long(0));
				cn.fields.add(fn);
			}
			cn.fields.add(new FieldNode(ACC_PUBLIC, RETURN_VALUE_NAME, ShadowFieldRewriter.TAINT_TYPE.getDescriptor(), null, new Long(0)));
		}
	}
}
