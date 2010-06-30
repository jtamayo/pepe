package test.edu.stanford.pepe;

import edu.stanford.pepe.runtime.TransactionId;
import junit.framework.TestCase;

public class TestTransactionId extends TestCase {

	public void testIncrementTransactionId() throws Exception {
		TransactionId tid1 = TransactionId.newTransaction();
		TransactionId tid2 = TransactionId.newTransaction();
		assertEquals(1, TransactionId.getTransactionId(tid1.getNextTaint()));
		assertEquals(2, TransactionId.getTransactionId(tid2.getNextTaint()));
	}
	
	public void testIncrementQueryId() throws Exception {
		TransactionId tid = TransactionId.newTransaction();
		for (int i = 0; i < 32; i++) {
			long taint = tid.getNextTaint();
			// Check that it can generate all distinct dependencies
			assertTrue(TransactionId.isDependentOnQuery(taint, i));
		}
		// After it saturates, it always depends on the last query
		assertTrue(TransactionId.isDependentOnQuery(tid.getNextTaint(), 31));
	}
	
}
