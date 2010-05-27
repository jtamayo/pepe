import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import util.Util;


public class TestJDBC2 {

	ExecutorService executor = Executors.newFixedThreadPool(10);

	String sql = "select max(img_size), min(img_bits) from wiki.image";
	String parameters = "abcde";
	
	AtomicLong sum = new AtomicLong();
	
	public static void main(String[] args) throws Exception {
//		final Connection conn = getOracleConnection();
		final Connection conn = Util.getMySqlConnection();
		
		long exec = 0;
		long simple = 0;
		TestJDBC2 testJDBC2 = new TestJDBC2();
		for (int i = 0; i < 1000; i++) {
			long doSimple = testJDBC2.doSimple(conn);
			simple += doSimple;
			System.out.print("Partial: simple " + doSimple);
			long doExecutors = testJDBC2.doExecutors(conn);
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
//			ps.setString(1, "%" + parameters.charAt(i) + "%");
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				sum.addAndGet(rs.getLong(1) + rs.getLong(2));
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
//					ps.setString(1, "%" + parameters.charAt(index) + "%");
					ResultSet rs = ps.executeQuery();
					Long localSum = 0l;
					if (rs.next()) {
						localSum = rs.getLong(1) + rs.getLong(2);
					}
					sum.addAndGet(localSum);
					rs.close();
					ps.close();
					return localSum;
				}
			};
			futures.add(executor.submit(cInfo));
		}
		
		for (Future<Long> future : futures) {
			future.get();
		}
		return (System.nanoTime() - start)/1000000;
	}
		
}
