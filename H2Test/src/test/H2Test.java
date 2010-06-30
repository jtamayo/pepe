package test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class H2Test {
	private final Connection conn;

	public H2Test(Connection conn) {
		this.conn = conn;
	}

	public static void main(String[] args) throws Exception {
		Class.forName("org.h2.Driver");
		Connection conn = DriverManager.getConnection("jdbc:h2:~/test", "sa", "");
		H2Test test = new H2Test(conn);
		System.out.println("Buying");
		test.buy(1, new int[]{1,3,3, 2, 2, 1});
		System.out.println("Finished");
//		QueryLogger.compile();
		conn.close();
	}

	public void buy(int customerId, int[] productIds) throws SQLException {
		Customer customer = findCustomer(customerId);
		List<Product> products = new ArrayList<Product>();
		for (int productId : productIds) {
			products.add(findProduct(productId));
		}
		Order order = new Order(-1, customer);
		for (Product product : products) {
			OrderItem item = new OrderItem(-1, order, product);
			order.addItem(item);
		}
		Balance balance = new Balance(-1, customer, order.computeTotal());
		save(order);
		save(balance);
	}

	private void save(Balance balance) throws SQLException {
		// CREATE TABLE BALANCES(ID INT AUTO_INCREMENT PRIMARY KEY, CUSTOMER INT, BALANCE INT);
		PreparedStatement ps = conn.prepareStatement("INSERT INTO BALANCES (CUSTOMER, BALANCE) VALUES (?,?)");
		try {
			ps.setInt(1, balance.customer.id);
			ps.setInt(2, balance.balance);
			ps.executeUpdate();
		} finally {
			ps.close();
		}
	}

	private void save(Order order) throws SQLException {
		// CREATE TABLE ORDERS(ID INT PRIMARY KEY, CUSTOMER INT);
		PreparedStatement ps = conn.prepareStatement("INSERT INTO ORDERS (CUSTOMER) VALUES (?)");
		try {
			ps.setInt(1, order.customer.id);
			ps.executeUpdate();
			for (OrderItem item : order.getItems()) {
				save(item);
			}
		} finally {
			ps.close();
		}
	}

	private void save(OrderItem item) throws SQLException {
		//CREATE TABLE ORDER_ITEM(ID INT AUTO_INCREMENT PRIMARY KEY, ORDER_ID INT, PRODUCT_ID INT);
		PreparedStatement ps = conn.prepareStatement("INSERT INTO ORDER_ITEM (ORDER_ID, PRODUCT_ID) VALUES (?,?)");
		try {
			ps.setInt(1, item.order.id);
			ps.setInt(2, item.product.id);
			ps.executeUpdate();
		} finally {
			ps.close();
		}

	}

	private Product findProduct(int productId) throws SQLException {
		//CREATE TABLE PRODUCTS(ID INT AUTO_INCREMENT PRIMARY KEY, DESCRIPTION VARCHAR(255), PRICE INT);
		PreparedStatement ps = conn.prepareStatement("SELECT ID, DESCRIPTION, PRICE FROM PRODUCTS WHERE ID = ?");
		ResultSet rs = null;
		try {
			ps.setInt(1, productId);
			rs = ps.executeQuery();
			if (rs.next()) {
				return new Product(rs.getInt(1), rs.getString(2), rs.getInt(3));
			} else {
				return null;
			}
		} finally {
			if (rs != null) rs.close();
			ps.close();
		}
	}

	private Customer findCustomer(int customerId) throws SQLException {
		//CREATE TABLE CUSTOMERS(ID INT AUTO_INCREMENT PRIMARY KEY, FULLNAME VARCHAR(255));
		PreparedStatement ps = conn.prepareStatement("SELECT ID, FULLNAME FROM CUSTOMERS WHERE ID = ?");
		ResultSet rs = null;
		try {
			ps.setInt(1, customerId);
			rs = ps.executeQuery();
			if (rs.next()) {
				return new Customer(rs.getInt(1), rs.getString(2));
			} else {
				return null;
			}
		} finally {
			ps.close();
			if (rs != null) rs.close();
		}
	}
}
