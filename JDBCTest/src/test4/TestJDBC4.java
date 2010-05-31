package test4;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import util.Util;

public class TestJDBC4 {

	static final String sql = "select img_bits, img_width, img_name from image where img_name like ?";
//	static final String sql = "select img_bits, img_width, max(img_name) from image where img_name like ? group by img_bits, img_width";
	static final String parameters = "ABCDEFG";
	static final int queriesPerTask = 2;
	static final int tasksPerBatch = 4;
	static final int batches = 4;
	
	static final int workerThreads = 2; // Number of parallel connections doing work
	
	// Useless counter, just to do something with the results and check consistency.
	static AtomicLong sum = new AtomicLong();
	
	ExecutorService queryExecutor = Executors.newFixedThreadPool(workerThreads);
	ExecutorService taskExecutor = Executors.newFixedThreadPool(workerThreads);
	
	// Execution times per task
	List<Long> serialExecutionTimes = new ArrayList<Long>(batches * tasksPerBatch); 
	List<Long> parallelExecutionTimes = new ArrayList<Long>(batches * tasksPerBatch); 
	
	public static void main(String[] args) throws Exception {
		TestJDBC4 testJDBC = new TestJDBC4();
		Thread t = Thread.currentThread();
		testJDBC.doTest();
	}

	private void doTest() throws Exception, InterruptedException {
		ConnectionPool pool = new ConnectionPool(workerThreads);
		
		long parallel = 0;
		long simple = 0;
		for (int batch = 0; batch < batches; batch++) {
			System.out.println("Starting batch " + batch);
			
			sum.set(0);
			long doSimple = doSimple(pool);
			long valueSimple = sum.get();
			simple += doSimple;
			System.out.print("Simple " + doSimple);

			sum.set(0);
			long doExecutors = doExecutors(pool);
			long valueExec = sum.get();
			parallel += doExecutors;
			System.out.println(" exec " + doExecutors);
			assert valueExec == valueSimple : "Both tasks should produce the same result";
			
		}
		
		long totalSerialTime = 0;
		for (long l : serialExecutionTimes) {
			totalSerialTime += l;
		}
		System.out.print("Average latency: serial="  + totalSerialTime / serialExecutionTimes.size());
		long totalParTime = 0;
		for (long l : parallelExecutionTimes) {
			totalParTime += l;
		}
		System.out.println(" parallel=" + totalParTime / parallelExecutionTimes.size());
		
		
		pool.close();
		queryExecutor.shutdown();
		taskExecutor.shutdown();
		queryExecutor.awaitTermination(100, TimeUnit.MILLISECONDS);
		taskExecutor.awaitTermination(100, TimeUnit.MILLISECONDS);
	}

	private long doExecutors(ConnectionPool pool) throws Exception {
		Connection c = pool.get();
		long start = System.nanoTime();
		for (int i = 0; i < tasksPerBatch; i++) {
			ParallelTask task = new ParallelTask(c, new String[]{"A", "B"}, queryExecutor);
			Long taskTime = task.call();
			parallelExecutionTimes.add(taskTime);
		}
		pool.release(c);
		return (System.nanoTime() - start) / 1000000;
	}

	private long doSimple(ConnectionPool pool) throws InterruptedException, ExecutionException {
		// Creates 12 serial tasks, and submits them to the task executor.
		long start = System.nanoTime();
		List<Future<Long>> futures = new ArrayList<Future<Long>>();
		for (int i = 0; i < tasksPerBatch; i++) {
			SerialTask task = new SerialTask(new String[]{"A","B"}, pool);
			Future<Long> future = taskExecutor.submit(task);
			futures.add(future);
		}
		for (Future<Long> f : futures) {
			long taskTime = f.get();
			serialExecutionTimes.add(taskTime);
		}
		return (System.nanoTime() - start) / 1000000;
	}

	public static long doComplicatedStuff(ResultSet rs) throws SQLException {
		long sum = 0;
		long size = rs.getLong(1);
		long bits = rs.getLong(2);
		String name = rs.getString(3);
		for (int i = 0; i < 3; i++) {
			String[] parts = name.split(",.+");
			for (String part : parts) {
				sum += part.hashCode() + i;
				sum += size % parts.length;
			}
		}
		return sum;
	}
		
}

class ParallelTask implements Callable<Long> {

	private final String[] parameters;
	private final Connection c;
	private final ExecutorService executor;

	public ParallelTask(Connection c, String[] parameters, ExecutorService executor) {
		this.c = c;
		this.parameters = parameters;
		this.executor = executor;
	}
	
	@Override
	public Long call() throws Exception {
		long start = System.nanoTime();
		List<Future<Long>> results = new ArrayList<Future<Long>>(parameters.length);
		for (int i = 0; i < parameters.length; i++) {
			final int index = i; 
			Callable<Long> query = new Callable<Long>() {
				@Override
				public Long call() throws Exception {
					PreparedStatement ps = c.prepareStatement(TestJDBC4.sql);
					ps.setString(1,  parameters[index] + "%");
					ResultSet rs = ps.executeQuery();
					while (rs.next()) {
//						long size = rs.getLong(1);
//						long bits = rs.getLong(2);
//						TestJDBC4.sum.addAndGet(bits*size);
						TestJDBC4.sum.addAndGet(TestJDBC4.doComplicatedStuff(rs));
					}
					rs.close();
					ps.close();
					return 0L;
				}
			};
			results.add(executor.submit(query));
		}
		for (Future<Long> f : results) {
			f.get();
		}
		return (System.nanoTime() - start) / 1000000;
	}
	
}

class SerialTask implements Callable<Long> {
	
	private final String[] parameters;
	private final ConnectionPool pool;

	public SerialTask(String[] parameters, ConnectionPool pool) {
		this.parameters = parameters;
		this.pool = pool;
	}

	@Override
	public Long call() throws Exception {
		Connection c = pool.get();
		long start = System.nanoTime();
		for (int i = 0; i < parameters.length; i++) {
			PreparedStatement ps = c.prepareStatement(TestJDBC4.sql);
			ps.setString(1, parameters[i] + "%");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
//				long size = rs.getLong(1);
//				long bits = rs.getLong(2);
//				TestJDBC4.sum.addAndGet(bits*size);
				TestJDBC4.sum.addAndGet(TestJDBC4.doComplicatedStuff(rs));
			}
			rs.close();
			ps.close();
		}
		long elapsed = (System.nanoTime() - start)/ 1000000;
		pool.release(c);
		return elapsed;
	}
	
}

class ConnectionPool {
	List<Connection> connections = new ArrayList<Connection>();
	List<Boolean> connectionsInUse = new ArrayList<Boolean>();
	
	public ConnectionPool(final int size) throws Exception {
		for (int i = 0; i < size; i++) {
			final Connection conn = Util.getMySqlConnection();
//			final Connection conn = Util.getOracleConnection();
			connections.add(conn);
			connectionsInUse.add(false);
		}
	}
	
	public void close() throws SQLException {
		for (Connection c : connections) {
			c.close();
		}
	}

	synchronized public Connection get() throws SQLException {
		for( int i = 0; i < connections.size(); i++) {
			if (connectionsInUse.get(i) == false) {
				connectionsInUse.set(i, true);
				Connection connection = connections.get(i);
				connection.setAutoCommit(false);
				return connection;
			}
		}
		throw new RuntimeException("No more connections available");
	}
	
	synchronized public void release(Connection c) throws SQLException {
		for( int i = 0; i < connections.size(); i++) {
			if (c == connections.get(i)) {
				connectionsInUse.set(i, false);
				c.commit();
				c.setAutoCommit(true);
				return;
			}
		}
		throw new RuntimeException("This is not one of my connections!");
	}
}
