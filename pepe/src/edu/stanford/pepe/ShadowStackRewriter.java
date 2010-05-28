package edu.stanford.pepe;

import static edu.stanford.pepe.TaintProperties.SHADOW_FIELD_SIZE;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.stanford.aj.prep.FrameAnalyzer;
import edu.stanford.pepe.org.objectweb.asm.AnnotationVisitor;
import edu.stanford.pepe.org.objectweb.asm.Attribute;
import edu.stanford.pepe.org.objectweb.asm.ClassAdapter;
import edu.stanford.pepe.org.objectweb.asm.ClassVisitor;
import edu.stanford.pepe.org.objectweb.asm.Label;
import edu.stanford.pepe.org.objectweb.asm.MethodVisitor;
import edu.stanford.pepe.org.objectweb.asm.Opcodes;
import edu.stanford.pepe.org.objectweb.asm.tree.ClassNode;
import edu.stanford.pepe.org.objectweb.asm.tree.MethodNode;
import edu.stanford.pepe.org.objectweb.asm.tree.analysis.Frame;

public class ShadowStackRewriter {
	public static Logger logger = Logger.getLogger(ShadowStackRewriter.class.getName());
	{
		logger.setLevel(Level.INFO);
	}

	@SuppressWarnings("unchecked")
	public static void rewrite(ClassNode cn, ClassVisitor output) {
		// First output the entire class, except the methods and the visitEnd
		copyAllButMethods(cn, output);

		// Now instrument each method
		List<MethodNode> methods = cn.methods;

		for (MethodNode mn : methods) {
			Frame[] frames = FrameAnalyzer.getInstance().compute(cn.name, mn);
			MethodVisitor outputMethodVisitor = output.visitMethod(mn.access, mn.name, mn.desc, mn.signature,
					(String[]) mn.exceptions.toArray(new String[]{})); // This line would be called by the copyAllButMethods, but we're skipping it
			ShadowStackVisitor v = new ShadowStackVisitor(outputMethodVisitor, mn, frames, cn);
			mn.accept(v);
		}
		
		// Every method has been visited, all that remains is to call visitEnd on the output
		output.visitEnd();
	}

	/** Transfer all events except visitMethod and visitEnd. */
	private static void copyAllButMethods(final ClassNode root, final ClassVisitor output) {
		root.accept(new ClassAdapter(output) {
			@Override
			public MethodVisitor visitMethod(final int access, final String name, final String desc,
					final String signature, final String[] exceptions) {
				return null;
			}

			@Override
			public void visitEnd() {
			}
		});
	}
	
	static class ShadowStackVisitor implements MethodVisitor, Opcodes {
		
		public static final int WORKING_STACK_SIZE = 2;

		private final MethodNode mn;
		private final MethodVisitor output;
		private final Frame[] frames;
		private final int newMaxStack;
		private final int newMaxLocals;
		private final int shadowStackStart;

		private int inst = -1; // Start one before the instructions, because it's incremented before any visitXX method is executed

		private final ClassNode cn;

		public ShadowStackVisitor(MethodVisitor output, MethodNode mn, Frame[] frames, ClassNode cn) {
			this.output = output;
			this.mn = mn;
			this.frames = frames;
			this.cn = cn;
			// new locals will be: the old locals + shadow for old locals + shadow copy of the stack
			this.newMaxLocals = mn.maxLocals + SHADOW_FIELD_SIZE*mn.maxLocals + SHADOW_FIELD_SIZE*mn.maxStack;
			// The shadow stack will start right after the shadows for the locals
			this.shadowStackStart = mn.maxLocals + SHADOW_FIELD_SIZE*mn.maxLocals;
			// The stack is the old stack, plus our working variables
			this.newMaxStack = mn.maxStack + WORKING_STACK_SIZE;
		}
		
		@Override
		public void visitMaxs(int maxStack, int maxLocals) {
			output.visitMaxs(newMaxStack, newMaxLocals);
		}



		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			return output.visitAnnotation(desc, visible);
		}



		@Override
		public AnnotationVisitor visitAnnotationDefault() {
			return output.visitAnnotationDefault();
		}



		@Override
		public void visitAttribute(Attribute attr) {
			output.visitAttribute(attr);
		}



		@Override
		public void visitCode() {
			output.visitCode();
		}



		@Override
		public void visitEnd() {
	        if (inst != (mn.instructions.size() - 1)) {
	            throw new RuntimeException(
	                    "index=" + inst + ", instr count=" + mn.instructions.size());
	        }
			output.visitEnd();
		}



		@Override
		public void visitFieldInsn(int opcode, String owner, String name, String desc) {
			inst++;
			output.visitFieldInsn(opcode, owner, name, desc);
		}



		@Override
		public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
			inst++;
			output.visitFrame(type, nLocal, local, nStack, stack);
		}



		@Override
		public void visitIincInsn(int var, int increment) {
			inst++;
			output.visitIincInsn(var, increment);
		}



		@Override
		public void visitInsn(int opcode) {
			inst++;
			// The stack depth before instruction inst. Long and doubles count as only one value.
			final int stackSize = frames[inst].getStackSize();
			final int shadowStackIndex = SHADOW_FIELD_SIZE*(stackSize) + shadowStackStart;
			
			switch (opcode) {
			case NOP:
				// just pass it along
				output.visitInsn(opcode);
				break;
			case ACONST_NULL:
			case ICONST_M1:
			case ICONST_0:
			case ICONST_1:
			case ICONST_2:
			case ICONST_3:
			case ICONST_4:
			case ICONST_5:
			case LCONST_0:
			case LCONST_1:
			case FCONST_0:
			case FCONST_1:
			case FCONST_2:
			case DCONST_0:
			case DCONST_1:
				// Constants are never tainted
				output.visitInsn(LCONST_0);
				output.visitVarInsn(LSTORE, shadowStackIndex);
				output.visitInsn(opcode);
				break;
			case IALOAD:
			case LALOAD:
			case FALOAD:
			case DALOAD:
			case AALOAD:
			case BALOAD:
			case CALOAD:
			case SALOAD:
			case IASTORE:
			case BASTORE:
			case CASTORE:
			case SASTORE:
			case LASTORE:
			case FASTORE:
			case DASTORE:
			case AASTORE:
			case POP:
			case POP2:
			case DUP:
			case DUP_X1:
			case DUP_X2:
			case DUP2:
			case DUP2_X1:
			case DUP2_X2:
			case SWAP:
			case IADD:
			case LADD:
			case FADD:
			case DADD:
			case ISUB:
			case LSUB:
			case FSUB:
			case DSUB:
			case IMUL:
			case LMUL:
			case FMUL:
			case DMUL:
			case IDIV:
			case LDIV:
			case FDIV:
			case DDIV:
			case IREM:
			case LREM:
			case FREM:
			case DREM:
			case INEG:
			case LNEG:
			case FNEG:
			case DNEG:
			case ISHL:
			case LSHL:
			case ISHR:
			case LSHR:
			case IUSHR:
			case LUSHR:
			case IAND:
			case LAND:
			case IOR:
			case LOR:
			case IXOR:
			case LXOR:
			case I2L:
			case I2F:
			case I2D:
			case L2I:
			case L2F:
			case L2D:
			case F2I:
			case F2L:
			case F2D:
			case D2I:
			case D2L:
			case D2F:
			case I2B:
			case I2C:
			case I2S:
			case LCMP:
			case FCMPL:
			case FCMPG:
			case DCMPL:
			case DCMPG:
			case IRETURN:
			case LRETURN:
			case FRETURN:
			case DRETURN:
			case ARETURN:
			case RETURN:
			case ARRAYLENGTH:
			case ATHROW:
			case MONITORENTER:
			case MONITOREXIT:
				output.visitInsn(opcode);
				break;

			default:
				throw new RuntimeException("visitInst should not receive opcode " + opcode);
			}
		}



		@Override
		public void visitIntInsn(int opcode, int operand) {
			inst++;
			output.visitIntInsn(opcode, operand);
		}



		@Override
		public void visitJumpInsn(int opcode, Label label) {
			inst++;
			output.visitJumpInsn(opcode, label);
		}



		@Override
		public void visitLabel(Label label) {
			inst++;
			output.visitLabel(label);
		}



		@Override
		public void visitLdcInsn(Object cst) {
			inst++;
			output.visitLdcInsn(cst);
		}



		@Override
		public void visitLineNumber(int line, Label start) {
			inst++;
			output.visitLineNumber(line, start);
		}



		@Override
		public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
			output.visitLocalVariable(name, desc, signature, start, end, index);
		}



		@Override
		public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
			inst++;
			output.visitLookupSwitchInsn(dflt, keys, labels);
		}



		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String desc) {
			inst++;
			output.visitMethodInsn(opcode, owner, name, desc);
		}



		@Override
		public void visitMultiANewArrayInsn(String desc, int dims) {
			inst++;
			output.visitMultiANewArrayInsn(desc, dims);
		}



		@Override
		public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
			return output.visitParameterAnnotation(parameter, desc, visible);
		}



		@Override
		public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
			inst++;
			output.visitTableSwitchInsn(min, max, dflt, labels);
		}



		@Override
		public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
			output.visitTryCatchBlock(start, end, handler, type);
		}



		@Override
		public void visitTypeInsn(int opcode, String type) {
			inst++;
			output.visitTypeInsn(opcode, type);
		}



		@Override
		public void visitVarInsn(int opcode, int var) {
			inst++;
			output.visitVarInsn(opcode, var);
		}
		
	}
}
