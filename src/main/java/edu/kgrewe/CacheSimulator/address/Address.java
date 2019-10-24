package edu.kgrewe.CacheSimulator.address;

/**
 * Holds values for an address.
 * 
 * @author kgrewe
 *
 */
public class Address {
	private int id;
	private String address;

	public Address(String addr, int id) {
		setAddress(addr);
		setId(id);
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
