package test;

public class Balance {
	public final int balance;
	public final int id;
	public final Customer customer;

	public Balance(int id, Customer customer, int balance) {
		this.id = id;
		this.customer = customer;
		this.balance = balance;
	}
}
