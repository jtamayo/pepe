/*
 * FrameAnalyzer.java
 *
 * Copyright 2007 Nathan Bronson and Stanford University.
 */

package edu.stanford.aj.prep;

import edu.stanford.pepe.org.objectweb.asm.Type;
import edu.stanford.pepe.org.objectweb.asm.tree.AbstractInsnNode;
import edu.stanford.pepe.org.objectweb.asm.tree.MethodNode;
import edu.stanford.pepe.org.objectweb.asm.tree.analysis.Analyzer;
import edu.stanford.pepe.org.objectweb.asm.tree.analysis.AnalyzerException;
import edu.stanford.pepe.org.objectweb.asm.tree.analysis.BasicInterpreter;
import edu.stanford.pepe.org.objectweb.asm.tree.analysis.BasicValue;
import edu.stanford.pepe.org.objectweb.asm.tree.analysis.Frame;
import edu.stanford.pepe.org.objectweb.asm.tree.analysis.SimpleVerifier;
import edu.stanford.pepe.org.objectweb.asm.tree.analysis.Value;
import edu.stanford.pepe.org.objectweb.asm.util.TraceMethodVisitor;

/**
 * Given a {@link MethodNode}, returns an array of {@link Frame} corresponding one-to-one with
 * the method's instructions.
 */
public class FrameAnalyzer {
	
    public static final String OBJECT_TYPE = "java/lang/Object";
    public static final String OBJECT_SIG = "L"+OBJECT_TYPE+";";
    
    
    private static final FrameAnalyzer instance = new FrameAnalyzer();

    /** Returns the active {@link FrameAnalyzer} implementation. */
    public static FrameAnalyzer getInstance() {
        return instance;
    }

    /**
     * {@link SimpleInterpreter} is similar to {@link SimpleVerifier}, but if it does not have
     * information about whether or not a cast is allowed it is permissive instead of trying to
     * load the class.
     */
    private static class SimpleInterpreter extends BasicInterpreter {
        private static final Value NULL_VALUE = new BasicValue(Type.getObjectType("null"));

        private final String _classType;

        private SimpleInterpreter(final String classType) {
            _classType = classType;
        }

        @Override
        public Value newValue(final Type type) {
            if (isRef(type)) {
                // BasicInterpreter will merge them, we don't want to
                return new BasicValue(type);
            }
            else {
                return super.newValue(type);
            }
        }

        @Override
        public Value binaryOperation(final AbstractInsnNode insn,
                                     final Value value1,
                                     final Value value2) throws AnalyzerException
        {
            if (insn.getOpcode() == AALOAD) {
                final String desc = ((BasicValue)value1).getType().getDescriptor();
                if (desc.equals("Lnull;")) {
                    // Another path to this frame will have the correct array type.  For now
                    // we return the NULL type, which will merge() with any other type. 
                    return NULL_VALUE;
                }
                if (desc.charAt(0) != '[') {
                    throw new RuntimeException("AALOAD.value1.desc was "+desc);
                }
                return newValue(Type.getType(desc.substring(1)));
            }
            else {
                return super.binaryOperation(insn, value1, value2);
            }
        }

        /** Returns true iff <tt>t</tt> is an array reference or object reference type. */
        private static boolean isRef(final Type t) {
            return t != null && (t.getSort() == Type.OBJECT || t.getSort() == Type.ARRAY);
        }

        @Override
        public Value merge(final Value v, final Value w) {
            // v arrives from one branch, w from another
            if (v.equals(w)) {
                return v;
            }
            // null is compatible with all other reference types
            if (v.equals(NULL_VALUE)) {
                return w;
            }
            if (w.equals(NULL_VALUE)) {
                return v;
            }
            // WTF are we supposed to do here?
            if (v == BasicValue.RETURNADDRESS_VALUE || w == BasicValue.RETURNADDRESS_VALUE) {
                System.out.println(_classType + ": unexpected merge of " + v + " and " + w);
                return BasicValue.RETURNADDRESS_VALUE;
            }
            // uninitialized dominates all other types
            if (v == BasicValue.UNINITIALIZED_VALUE || w == BasicValue.UNINITIALIZED_VALUE) {
                return BasicValue.UNINITIALIZED_VALUE;
            }
            // java.lang.Object dominates all other types except unitialized
            if (v.equals(BasicValue.REFERENCE_VALUE) || w.equals(BasicValue.REFERENCE_VALUE)) {
                return BasicValue.REFERENCE_VALUE;
            }

            // We're going to guess, but at least we can guess the most specific possible
            // given no other information.
            final Value guess;
            final Type vt = ((BasicValue)v).getType();
            final Type wt = ((BasicValue)w).getType();
            if (vt == null || wt == null) {
                System.out.println(" v = " + v);
                System.out.println(" vt = " + vt);
                System.out.println(" w = " + w);
                System.out.println(" wt = " + wt);
            }
            if (vt.getSort() == Type.ARRAY && wt.getSort() == Type.ARRAY) {
                final int dim = Math.min(vt.getDimensions(), wt.getDimensions());
                final StringBuilder desc = new StringBuilder();
                for (int i = 0; i < dim; ++i) {
                    desc.append('[');
                }
                desc.append(OBJECT_SIG);
                guess = new BasicValue(Type.getType(desc.toString()));
            }
            else {
                guess = BasicValue.REFERENCE_VALUE;
            }

            // TODO: do we need to be more sophisticated?
            //System.out.println("assuming common supertype of "+v+" and "+w+" is "+guess);
            return guess;
        }
    }

    /**
     * Returns an array of {@link Frame} corresponding to the bytecode state at each
     * instruction in <tt>input</tt>.  The returned array will have
     * <tt>input.instructions.size()</tt> elements.
     */
    public Frame[] compute(final String classType, final MethodNode input) {
        final Analyzer a;
        try {
            a = new Analyzer(new SimpleInterpreter(classType));
            a.analyze(classType, input);
            final Frame[] result = a.getFrames();
//            if (classType.equals("spec/benchmarks/_213_javac/NewArrayExpression") && input.name.equals("inline")) {
//                for (int i = 0; i < input.instructions.size(); ++i) {
//                    System.out.println("input[" + i + "] = " + input.instructions.get(i));
//                }
//                for (int i = 0; i < result.length; ++i) {
//                    System.out.println("result[" + i + "] = " + result[i]);
//                }
//            }
            return result;
        }
        catch (final AnalyzerException xx) {
            final TraceMethodVisitor t = new TraceMethodVisitor();
            input.accept(t);
            System.out.println("ORIGINAL CODE:");
            System.out.println(t.getText());
//            System.out.println("INSN[350] = " + input.instructions.get(350));
//            System.out.println("INSN[351] = " + input.instructions.get(351));
//            System.out.println("INSN[352] = " +
//                               ((LineNumberNode)input.instructions.get(352)).line);
//            System.out.println("INSN[353] = " + input.instructions.get(353));
//            System.out.println("INSN[354] = " + input.instructions.get(354));
            throw new RuntimeException(xx);
        }
    }

    /** Returns the type signature for the value on the top of the stack. */
    public Type stackTopType(final Frame frame) {
        final BasicValue v = (BasicValue)frame.getStack(frame.getStackSize() - 1);
        return v.getType();
    }

    /**
     * Returns the type signature for an array of primitives, an array of objects, or a
     * <tt>java.lang.Object</tt>. Assumes that the specified <tt>frame</tt> contains the
     * arguments for a call to {@link System#arraycopy(Object,int,Object,int,int)}. Only returns
     * {@link RewriteConstants#OBJECT_SIG} if a frame analysis of this method is unable to
     * deduce the types of both src and dest.
     */
    public String arraycopyArgSig(final Frame frame) {
        // Note that frame.getStack() treats longs and doubles as 1 slot, unlike everybody else.

        // Object src, int srcPos, Object dest, int destPos, int length
        final BasicValue srcV = (BasicValue)frame.getStack(frame.getStackSize() - 5);
        final BasicValue destV = (BasicValue)frame.getStack(frame.getStackSize() - 3);
        final String srcT = srcV.getType().getInternalName();
        final String destT = destV.getType().getInternalName();
        if (srcT.charAt(0) != '[' || destT.charAt(0) != '[') {
            // array must have been passed through an Object reference
            return OBJECT_SIG;
        }
        if (srcT.length() == 2 || destT.length() == 2) {
            // at least one is an array of primitives
            if (srcT.equals(destT)) {
                // expected
                return srcT;
            }
            else {
                // invalid copy, but to preserve semantics this shouldn't generate an error
                // until run time
                return OBJECT_SIG;
            }
        }
        else {
            // both arrays are of objects
            return "[" + OBJECT_SIG;
        }
    }
}
