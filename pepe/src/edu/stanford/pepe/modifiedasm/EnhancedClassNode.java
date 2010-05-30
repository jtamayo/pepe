package edu.stanford.pepe.modifiedasm;

import java.util.HashMap;
import java.util.Map;

import edu.stanford.pepe.org.objectweb.asm.FieldVisitor;
import edu.stanford.pepe.org.objectweb.asm.tree.ClassNode;
import edu.stanford.pepe.org.objectweb.asm.tree.FieldNode;

public class EnhancedClassNode extends ClassNode {

	public Map<String, FieldNode> fieldsByName;
	
	public EnhancedClassNode() {
		fieldsByName = new HashMap<String, FieldNode>();
	}
	
	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		super.visitField(access, name, desc, signature, value);
		// XXX I should not depend on something as arbitrary as the FieldNode being the latest thing added to the fields List
		FieldNode fn = (FieldNode) fields.get(fields.size() - 1);
		fieldsByName.put(name, fn);
		return fn;
	}
	
}
