package test;

public class OrderItem {
	public final Order order;
	public final int id;
	public final Product product;

	public OrderItem(int id, Order order, Product product) {
		this.id = id;
		this.order = order;
		this.product = product;
	}
}
