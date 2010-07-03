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
 * 
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
		List<FieldNode> shadowFields = new ArrayList<FieldNode>();
		for (FieldNode fn : (List<FieldNode>) cn.fields) {
			final int fieldAccess = fn.access;
			final boolean isStatic = (fieldAccess & ACC_STATIC) != 0;
			final boolean isFinal = (fieldAccess & ACC_FINAL) != 0;
			if (isStatic && isFinal) {
				// static final fields are instrumented, even though it's not necessary,
				// because at the caller side it's impossible to know whether a field is
				// final, and thus it's impossible to determine whether it is shadowed or not.
				int newAccess = ACC_STATIC | ACC_PUBLIC + ACC_FINAL + ACC_SYNTHETIC;
				String newName = getShadowFieldName(fn.name);
				FieldNode shadowFn = new FieldNode(newAccess, newName, TAINT_TYPE.getDescriptor(), null, new Long(0));
				shadowFields.add(shadowFn);
				logger.finer("Instrumenting static final field " + cn.name + " " + fn.name);
			} else if (InstrumentationPolicy.isFieldInstrumentable(cn.name, fn.name, isStatic)) {
				// Section 4.7.6 in the JVMS, if it's not in the source code it's synthetic
				// A non-static field should be transient, so it is not serialized
				int newAccess = isStatic ? (ACC_STATIC | ACC_PUBLIC | ACC_SYNTHETIC)
						: (ACC_TRANSIENT | ACC_PUBLIC | ACC_SYNTHETIC);
				String newName = getShadowFieldName(fn.name);
				FieldNode shadowFn = new FieldNode(newAccess, newName, TAINT_TYPE.getDescriptor(), null, null);
				shadowFields.add(shadowFn);
				logger.finer("Instrumenting field " + cn.name + " " + fn.name);

			} else {
				logger.finer("Skipping field " + cn.name + " " + fn.name);
			}

		}
		// Add the mark to determine that this class has been instrumented
		shadowFields.add(new FieldNode(ACC_STATIC + ACC_PUBLIC + ACC_FINAL + ACC_SYNTHETIC, TAINT_MARK, TAINT_TYPE.getDescriptor(), null, new Long(0)));
		
		cn.fields.addAll(shadowFields);
		
		// TODO: How about the this$0 parameter, equivalent to the (OuterClass) this pointer in inner classes?
	}


	public static String getShadowFieldName(String fieldName) {
		// TODO: Determine where to put this method, because it's invoked from ShadowStackRewriter, and 
		// perhaps that class should not depend on this one.
		return fieldName + TAINT_SUFFIX;
	}

}
