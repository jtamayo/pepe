package edu.stanford.pepe;

public class TestIntegerCreate {

	public TestIntegerCreate(int a) {
	}
	
	public TestIntegerCreate() {
		this(new Integer("".hashCode()));
	}
	
}

class A {
	
	int k;
	
	A(Thread t, int i) {
		
	}
	
	static A get() {
		return new A(null, 0);
	}
}

class B extends A {
	B() {
		super(Thread.currentThread(), A.get().k = 9);
	}
	
	public static int computeInt(){
		return 9;
	}
	
}
