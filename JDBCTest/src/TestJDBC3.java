import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import util.Util;


public class TestJDBC3 {

	ExecutorService executor = Executors.newFixedThreadPool(2);

	String sql = "select img_size, img_bits, img_name from wiki.image where img_name like ?";
	String parameters = "ABCDEF";
	
	AtomicLong sum = new AtomicLong();
	
	public static void main(String[] args) throws Exception {
//		final Connection conn = getOracleConnection();
		final Connection conn = Util.getMySqlConnection();
		
		TestJDBC3 testJDBC2 = new TestJDBC3();
		testJDBC2.doTest(conn, testJDBC2);
	}

	private void doTest(final Connection conn, TestJDBC3 testJDBC2) throws Exception, InterruptedException {
		long exec = 0;
		long simple = 0;
		for (int i = 0; i < 1000; i++) {
			sum.set(0);
			long doSimple = testJDBC2.doSimple(conn);
			long valueSimple = sum.get();
			simple += doSimple;
			System.out.print("Partial: simple " + doSimple);
			sum.set(0);
			long doExecutors = testJDBC2.doExecutors(conn);
			long valueExec = sum.get();
			assert valueExec == valueSimple : "Both tasks should produce the same result";
			exec += doExecutors;
			System.out.println(" exec " + doExecutors);
		}
		testJDBC2.executor.shutdown();
		testJDBC2.executor.awaitTermination(100, TimeUnit.MILLISECONDS);
		System.out.println("Simple: " + simple + " exec: " + exec);
	}

	public long doSimple(Connection conn) throws Exception {

		long start = System.nanoTime();

		for (int i = 0; i < parameters.length(); i++) {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, parameters.charAt(i) + "%");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				sum.addAndGet(doComplicatedStuff(rs));
			}
			rs.close();
			ps.close();
		}
		return (System.nanoTime() - start) / 1000000;

	}

	public long doExecutors(final Connection conn) throws Exception {

		ArrayList<Future<Long>> futures = new ArrayList<Future<Long>>(100);

		long start = System.nanoTime();
		
		for (int i = 0; i < parameters.length(); i++) {
			final int index = i; // So that I can use it from the inner class
			Callable<Long> cInfo = new Callable<Long>() {
				@Override
				public Long call() throws Exception {
					PreparedStatement ps = conn.prepareStatement(sql);
					ps.setString(1, parameters.charAt(index) + "%");
					ResultSet rs = ps.executeQuery();
					Long localSum = 0l;
					while (rs.next()) {
						localSum = doComplicatedStuff(rs);
//						sum.addAndGet(localSum);
					}
					rs.close();
					ps.close();
					return localSum;
				}
			};
			futures.add(executor.submit(cInfo));
		}
		
		for (Future<Long> future : futures) {
			sum.addAndGet(future.get());
		}
		return (System.nanoTime() - start)/1000000;
	}
	
	public long doComplicatedStuff(ResultSet rs) throws SQLException {
		long sum = 0;
		long size = rs.getLong(1);
		long bits = rs.getLong(2);
		String name = rs.getString(3);
		for (int i = 0; i < 10*bits; i++) {
			String[] parts = name.split(",.+");
			for (String part : parts) {
				sum += part.hashCode() + i;
				sum += size % parts.length;
			}
		}
		return sum;
	}
		
}
