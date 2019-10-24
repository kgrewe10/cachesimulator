package edu.kgrewe.CacheSimulator.address;

/**
 * Extends address, represents an instruction fetch.
 * 
 * @author kgrewe
 *
 */
public class InstructionFetch extends Address {

	public InstructionFetch(String addr) {
		super(addr, 2);
	}

}
