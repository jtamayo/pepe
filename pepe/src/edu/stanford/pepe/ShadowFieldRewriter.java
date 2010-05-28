package edu.stanford.pepe;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.pepe.org.objectweb.asm.Opcodes;
import edu.stanford.pepe.org.objectweb.asm.Type;
import edu.stanford.pepe.org.objectweb.asm.tree.ClassNode;
import edu.stanford.pepe.org.objectweb.asm.tree.FieldNode;

/**
 * Adds the shadow fields to a {@link ClassNode} object.
 * @author jtamayo
 */
public class ShadowFieldRewriter implements Opcodes {

	public static Logger logger = Logger.getLogger(ShadowFieldRewriter.class.getName());
	{
		logger.setLevel(Level.INFO);
	}

	public static final Type TAINT_TYPE = Type.LONG_TYPE;
	public static final String TAINT_SUFFIX = "__$TAINT$__";
	public static final String TAINT_MARK = "__$INSTRUMENTED$__" + TAINT_SUFFIX;

	
	@SuppressWarnings("unchecked")
	public static void rewrite(ClassNode cn) {
		int classAccess = cn.access;
		boolean isInterface = (classAccess & ACC_INTERFACE) != 0;
		boolean isEnum = (classAccess & ACC_ENUM) != 0;
		boolean isAnnotation = (classAccess & ACC_ANNOTATION) != 0;
		

		if (isInterface || isAnnotation || isEnum) {
			logger.finer("Skipping field rewriting for class " + cn.name);
			return;
		}
		
		List<FieldNode> shadowFields = new ArrayList<FieldNode>();
		for (FieldNode fn : (List<FieldNode>) cn.fields) {
			int fieldAccess = fn.access;
			final boolean isStatic = (fieldAccess & ACC_STATIC) != 0;
			final boolean isFinal = (fieldAccess & ACC_FINAL) != 0;
			if (isStatic && isFinal) continue;
			if (!InstrumentationPolicy.isFieldInstrumentable(cn.name, fn.name, isStatic)) {
				System.out.println("Skipping " + cn.name + " " + fn.name);
				continue;
			}
			
			// Section 4.7.6 in the JVMS, if it's not in the source code it's synthetic
			// A non-static field should be transient, so it is not serialized
			int newAccess;
			if (isStatic) {
				newAccess = ACC_STATIC | ACC_PUBLIC | ACC_SYNTHETIC; 
			} else {
				newAccess = ACC_TRANSIENT | ACC_PUBLIC | ACC_SYNTHETIC; 
			}
			
			String newName = fn.name + TAINT_SUFFIX;
			FieldNode shadowFn = new FieldNode(newAccess, newName, TAINT_TYPE.getDescriptor(), null, null);
			shadowFields.add(shadowFn);
			logger.finer("Instrumenting field " + cn.name + " " + fn.name);
		}
		// Add the mark to de termine that this class has been instrumented
		shadowFields.add(new FieldNode(ACC_STATIC + ACC_PUBLIC + ACC_FINAL + ACC_SYNTHETIC, TAINT_MARK, TAINT_TYPE.getDescriptor(), null, new Long(0)));
		
		
		cn.fields.addAll(shadowFields);
		
		// TODO: What should we do with volatile fields? Should the shadow be volatile or not?
		// TODO: Make our fields transient, so they are not serialized - Change the computeSErialVersionUid
		// TODO: What do we do with static final fields? do we instrument them or not? are they really constants? is it easier to keep an always 0 shadow field? Doesn't happen in real life
		// TODO: How about the this$0 parameter, equivalent to the (OuterClass) this pointer in inner classes?
	
	}

}
