package edu.stanford.pepe;

import edu.stanford.pepe.org.objectweb.asm.ClassAdapter;
import edu.stanford.pepe.org.objectweb.asm.ClassVisitor;
import edu.stanford.pepe.org.objectweb.asm.MethodVisitor;

public class PrimitiveWrapperInstrumenter extends ClassAdapter {

	public PrimitiveWrapperInstrumenter(ClassVisitor cv) {
		super(cv);
	}
	
	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		if (!InstrumentationPolicy.isPrimitive(name)) {
			throw new IllegalArgumentException("Supplied class " + name + " is not a primitive wrapper");
		}
		super.visit(version, access, name, signature, superName, interfaces);
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		if (name.equals("<init>")) {
			MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
			return new PrimitiveWrapperConstructorVisitor(mv, access, name, desc);
		} else {
			return super.visitMethod(access, name, desc, signature, exceptions);
		}
	}

}
