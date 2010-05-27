package edu.stanford.pepe;

import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.pepe.org.objectweb.asm.ClassAdapter;
import edu.stanford.pepe.org.objectweb.asm.ClassVisitor;
import edu.stanford.pepe.org.objectweb.asm.FieldVisitor;
import edu.stanford.pepe.org.objectweb.asm.Opcodes;
import edu.stanford.pepe.org.objectweb.asm.Type;

/**
 * Adds shadow fields to every field.
 * 
 * @author jtamayo
 */
public class FieldInstrumentAdapter extends ClassAdapter implements Opcodes {
	
	public static Logger logger = Logger.getLogger("edu.stanford.pepe.FieldInstrumentAdapter");
	{
		logger.setLevel(Level.INFO);
	}

	public static final Type TAINT_TYPE = Type.LONG_TYPE;
	public static final String TAINT_SUFFIX = "__$TAINT$__";

	private boolean isAbstract;
	private boolean isInterface;
	private boolean isEnum;
	private boolean isAnnotation;
	private String className;

	public FieldInstrumentAdapter(ClassVisitor cv) {
		super(cv);
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		isInterface = (access & ACC_INTERFACE) != 0;
		isAbstract = (access & ACC_ABSTRACT) != 0;
		isEnum = (access & ACC_ENUM) != 0;
		isAnnotation = (access & ACC_ANNOTATION) != 0;
		className = name;
		super.visit(version, access, name, signature, superName, interfaces);
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		if (isInterface || isAnnotation || isEnum) {
			logger.finer("Skipping field " + className + " " + name);
			return super.visitField(access, name, desc, signature, value);
		}
		final boolean isStatic = (access & ACC_STATIC) != 0;
		int newAccess = (isStatic ? ACC_STATIC : 0) | ACC_TRANSIENT | ACC_PUBLIC | ACC_SYNTHETIC; // Section 4.7.6 in the JVMS, if it's not in the source code it's synthetic
		String newName = name + TAINT_SUFFIX;

		FieldVisitor fv = cv.visitField(newAccess, newName, TAINT_TYPE.getDescriptor(), null, null);
		logger.finer("Instrumenting field " + className + " " + name);
		if (fv != null) {
			fv.visitEnd();
		}
		
		return super.visitField(access, name, desc, signature, value);

		// TODO: What should we do with volatile fields? Should the shadow be volatile or not?
		// TODO: Make our fields transient, so they are not serialized - Change the computeSErialVersionUid
		// TODO: What do we do with static final fields? do we instrument them or not? are they really constants? is it easier to keep an always 0 shadow field? Doesn't happen in real life
		// TODO: How about the this$0 parameter, equivalent to the (OuterClass) this pointer in inner classes?
	}

}
