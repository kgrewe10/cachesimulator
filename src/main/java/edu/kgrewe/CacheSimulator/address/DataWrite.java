package edu.kgrewe.CacheSimulator.address;

/**
 * Extends address, represents a data write.
 * 
 * @author kgrewe
 *
 */
public class DataWrite extends Address {
	public DataWrite(String addr) {
		super(addr, 1);
	}
}
