package edu.stanford.pepe;

import java.util.HashMap;
import java.util.Map;

import edu.stanford.pepe.org.objectweb.asm.AnnotationVisitor;
import edu.stanford.pepe.org.objectweb.asm.Attribute;
import edu.stanford.pepe.org.objectweb.asm.ClassVisitor;
import edu.stanford.pepe.org.objectweb.asm.FieldVisitor;
import edu.stanford.pepe.org.objectweb.asm.Label;
import edu.stanford.pepe.org.objectweb.asm.MethodVisitor;

/**
 * Constructs a map from each method descriptor to the maximum number of locals
 * and stack size for each method. This is required because the ASM visitor api
 * does not allow the visitor to query information that comes later in a method.
 * 
 * @author jtamayo
 */
public class MaxSizeVisitor implements ClassVisitor {

	private Map<String, Integer> maxLocalsxMethod = new HashMap<String, Integer>();
	private Map<String, Integer> maxStackxMethod = new HashMap<String, Integer>();

	/**
	 * Returns the cached value of the maxLocals for the given method.
	 */
	public int getMaxLocals(String methodName, String methodDescriptor) {
		return maxLocalsxMethod.get(computeMethodKey(methodName, methodDescriptor));
	}

	/**
	 * Returns the cached value of the maxStack for the given method.
	 */
	public int getMaxStack(String methodName, String methodDescriptor) {
		return maxStackxMethod.get(computeMethodKey(methodName, methodDescriptor));
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		return null;
	}

	@Override
	public void visitAttribute(Attribute attr) {
	}

	@Override
	public void visitEnd() {
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		return null;
	}

	@Override
	public void visitInnerClass(String name, String outerName, String innerName, int access) {
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		return null;
	}

	@Override
	public void visitOuterClass(String owner, String name, String desc) {
	}

	@Override
	public void visitSource(String source, String debug) {
	}

	private static String computeMethodKey(String methodName, String methodDescriptor) {
		return methodName + " " + methodDescriptor;
	}

	class MaxSizeMethodVisitor implements MethodVisitor {

		private final String key;

		public MaxSizeMethodVisitor(String methodName, String methodDescriptor) {
			this.key = computeMethodKey(methodName, methodDescriptor);
		}

		@Override
		public void visitMaxs(int maxStack, int maxLocals) {
			maxStackxMethod.put(key, maxStack);
			maxLocalsxMethod.put(key, maxLocals);
		}

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			return null;
		}

		@Override
		public AnnotationVisitor visitAnnotationDefault() {
			return null;
		}

		@Override
		public void visitAttribute(Attribute attr) {
		}

		@Override
		public void visitCode() {
		}

		@Override
		public void visitEnd() {
		}

		@Override
		public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		}

		@Override
		public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
		}

		@Override
		public void visitIincInsn(int var, int increment) {
		}

		@Override
		public void visitInsn(int opcode) {
		}

		@Override
		public void visitIntInsn(int opcode, int operand) {
		}

		@Override
		public void visitJumpInsn(int opcode, Label label) {
		}

		@Override
		public void visitLabel(Label label) {
		}

		@Override
		public void visitLdcInsn(Object cst) {
		}

		@Override
		public void visitLineNumber(int line, Label start) {
		}

		@Override
		public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
		}

		@Override
		public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		}

		@Override
		public void visitMultiANewArrayInsn(String desc, int dims) {
		}

		@Override
		public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
			return null;
		}

		@Override
		public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
		}

		@Override
		public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
		}

		@Override
		public void visitTypeInsn(int opcode, String type) {
		}

		@Override
		public void visitVarInsn(int opcode, int var) {
		}
	}

}
