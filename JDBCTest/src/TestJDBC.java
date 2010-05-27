import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import util.Util;


public class TestJDBC {

	ExecutorService executor = Executors.newFixedThreadPool(10);

	String sql = "select SALARY, JOB_TITLE from EMPLOYEES E inner join JOBS J on E.JOB_ID = J.JOB_ID where EMPLOYEE_ID = ?";
	
	ConcurrentMap<String, AtomicLong> summary = new ConcurrentHashMap<String, AtomicLong>(20);

	public static void main(String[] args) throws Exception {
//		final Connection conn = getOracleConnection();
		final Connection conn = Util.getMySqlConnection();
		
		long exec = 0;
		long simple = 0;
		for (int i = 0; i < 1000; i++) {
			long doExecutors = new TestJDBC().doExecutors(conn);
			exec += doExecutors;
			long doSimple = new TestJDBC().doSimple(conn);
			simple += doSimple;
			System.out.println("Partial: simple " + doSimple + " exec " + doExecutors);
		}
		System.out.println("Simple: " + simple + " exec: " + exec);
	}

	public long doSimple(Connection conn) throws Exception {

		long start = System.nanoTime();

		for (int i = 100; i < 207; i++) {
			final int index = i; // So that I can use it from the inner class
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, index);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				SalaryInfo salary = new SalaryInfo();
				salary.salary = rs.getInt(1);
				salary.jobTitle = rs.getString(2);
				updateSummary(salary);
			}
			rs.close();
			ps.close();
		}
		return (System.nanoTime() - start) / 1000000;

	}

	public long doExecutors(final Connection conn) throws Exception {

		ArrayList<Future<SalaryInfo>> futures = new ArrayList<Future<SalaryInfo>>(100);

		long start = System.nanoTime();
		
		for (int i = 100; i < 207; i++) {
			final int index = i; // So that I can use it from the inner class
			Callable<SalaryInfo> cInfo = new Callable<SalaryInfo>() {
				@Override
				public SalaryInfo call() throws Exception {
					PreparedStatement ps = conn.prepareStatement(sql);
					ps.setInt(1, index);
					ResultSet rs = ps.executeQuery();
					if (rs.next()) {
						SalaryInfo salary = new SalaryInfo();
						salary.salary = rs.getInt(1);
						salary.jobTitle = rs.getString(2);
						updateSummary(salary);
						rs.close();
						ps.close();
						return salary;
					}
					rs.close();
					ps.close();
					return null;
				}
			};
			futures.add(executor.submit(cInfo));
		}
		
		for (Future<SalaryInfo> future : futures) {
			future.get();
		}
		return (System.nanoTime() - start)/1000000;
	}
	
	public void updateSummary(SalaryInfo s) {
		summary.putIfAbsent(s.jobTitle, new AtomicLong(0));
		AtomicLong salarySum = summary.get(s.jobTitle);
		salarySum.addAndGet((long) (s.salary * 100));
	}

}

class SalaryInfo {
	double salary;
	String jobTitle;
}