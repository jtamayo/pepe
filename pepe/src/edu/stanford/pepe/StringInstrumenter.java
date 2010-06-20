package edu.stanford.pepe;

import edu.stanford.pepe.org.objectweb.asm.ClassAdapter;
import edu.stanford.pepe.org.objectweb.asm.ClassVisitor;

public class StringInstrumenter extends ClassAdapter {

	public StringInstrumenter(ClassVisitor cv) {
		super(cv);
	}

}
