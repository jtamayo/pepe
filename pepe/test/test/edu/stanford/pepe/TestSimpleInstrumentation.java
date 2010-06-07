package test.edu.stanford.pepe;

import junit.framework.TestCase;
import edu.stanford.pepe.TaintCheck;
import edu.stanford.pepe.Tainter;

public class TestSimpleInstrumentation extends TestCase {
	public void testReturnValue() throws Exception {
		Tainter t = new Tainter(123);
		int tainted = t.getInt(4);
		long taint = TaintCheck.getTaint(12345);
		assertEquals(123, taint);
	}
}
