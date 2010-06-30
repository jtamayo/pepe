package test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {
	public final Customer customer;
	public final int id;
	private final ArrayList<OrderItem> items = new ArrayList<OrderItem>();

	public Order(int id, Customer customer) {
		this.id = id;
		this.customer = customer;
	}

	public void addItem(OrderItem item) {
		items.add(item);
	}

	public List<OrderItem> getItems() {
		return Collections.unmodifiableList(items);
	}

	public int computeTotal() {
		int total = 0;
		for (OrderItem item : items) {
			total += item.product.price;
		}
		return total;
	}
}
