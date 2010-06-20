package test.edu.stanford.pepe;

import junit.framework.TestCase;
import edu.stanford.pepe.TaintCheck;

public class TestSimpleInstrumentation extends TestCase {
	public void testSimpleTaint() throws Exception {
		final long taint = 987654l;
		int tainted = TaintCheck.taint(5, taint);
		long endTaint = TaintCheck.getTaint(tainted);
		assertEquals(taint, endTaint);
		String a = "Help" + endTaint;
	}

	public void testSimpleMethodInvocation() throws Exception {
		final long taint = 9873245879l;
		int tainted = TaintCheck.taint(6, taint);
		int alsoTainted = add3(tainted);
		long endTaint = TaintCheck.getTaint(alsoTainted);
		assertEquals(taint,endTaint);
		assertEquals(9, alsoTainted);
	}
	
	public void testSimpleObject() {
		final long taint = 23987469278364l;
		int tainted = TaintCheck.taint(8, taint);
		SimpleObject o = new SimpleObject(tainted + 5);
		long endTaint = TaintCheck.getTaint(o.value);
		assertEquals(taint, endTaint);
	}
	
	public void testAddConstant() {
		final long taint = 76;
		int tainted = TaintCheck.taint(98, taint);
		int alsoTainted = tainted + 4;
		long endTaint = TaintCheck.getTaint(alsoTainted);
		assertEquals(taint,endTaint);
	}
	
	public void testSimpleArray() {
		final long taint = 654;
		int tainted = TaintCheck.taint(7,taint);
		int[] array = new int[7];
		array[4] = tainted + 2;
		long endTaint = TaintCheck.getTaint(array[4]);
		assertEquals(taint,endTaint);
	}
	
	public void testMeetSameTID() {
		final long taint1 = 0xA0A0A0A000000010L;
		final long taint2 = 0x0202020200000010L;
		int tainted1 = TaintCheck.taint(3, taint1);
		int tainted2 = TaintCheck.taint(5, taint2);
		int merged = tainted1 + tainted2;
		long endTaint = TaintCheck.getTaint(merged);
		assertEquals(0xA2A2A2A200000010L, endTaint);
	}
	
	public void testMeetDifferentTID() {
		final long taint1 = 0xA0A0A0A000000010L;
		final long taint2 = 0x0202020200000020L;
		// Taint2 is more recent, so that's the one that should be returned
		int tainted1 = TaintCheck.taint(3, taint1);
		int tainted2 = TaintCheck.taint(5, taint2);
		int merged = tainted1 + tainted2;
		long endTaint = TaintCheck.getTaint(merged);
		assertEquals(0x0202020200000020L, endTaint);
	}
	
	public void testMeet32bitTaint() {
		// Check what happens if the TIDs are "negative"
		final long taint1 = 0xA0A0A0A0FF000010L;
		final long taint2 = 0x02020202FF000020L;
		int tainted1 = TaintCheck.taint(3, taint1);
		int tainted2 = TaintCheck.taint(5, taint2);
		int merged = tainted1 + tainted2;
		long endTaint = TaintCheck.getTaint(merged);
		assertEquals(0x02020202FF000020L, endTaint);
	}
	
	public void testMeetWithZero() {
		// Check that meeting with 0 returns the latest taint
		final long taint1 = 0xA0A0A0A0FF000010L;
		final long taint2 = 0x0L;
		int tainted1 = TaintCheck.taint(3, taint1);
		int tainted2 = TaintCheck.taint(5, taint2);
		int merged = tainted1 + tainted2;
		long endTaint = TaintCheck.getTaint(merged);
		assertEquals(0xA0A0A0A0FF000010L, endTaint);
	}
	
	public void testTaintedReference() {
		// Check that if a reference is tainted fields accessed through
		// that reference are tainted
		final long taint1 = 0x11111111F0000010L;
		SimpleObject original = new SimpleObject(18);
		SimpleObject tainted = TaintCheck.taint(original, taint1);
		assertEquals(0, TaintCheck.getTaint(original.value));
		assertEquals(taint1, TaintCheck.getTaint(tainted.value));
	}
	
	public void testIntegerWrapper() throws Exception {
		final long taint = 0x11111111F0000010L;
		int taintedInt = TaintCheck.taint(678, taint);
		Integer wrapper = new Integer(taintedInt);
		assertEquals(5, wrapper.intValue());
		assertEquals(taint, TaintCheck.getTaint(wrapper.intValue()));
		
	}
	
	public int add3(int a) {
		return a + 3;
	}
	
	private static class SimpleObject {
		private final int value;

		public SimpleObject(int value) {
			this.value = value;
		}
	}
}
