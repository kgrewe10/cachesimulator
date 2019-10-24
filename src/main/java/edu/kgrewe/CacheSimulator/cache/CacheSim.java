package edu.kgrewe.CacheSimulator.cache;

import java.text.DecimalFormat;
import java.util.ArrayList;

import edu.kgrewe.CacheSimulator.address.Address;

/**
 * Runs a cache hit/miss simulation.
 * 
 * @author kgrewe
 *
 */
public class CacheSim {
	private Cache cacheArray[];
	private int unifiedMisses;
	private int dataMisses;
	private int instrMisses;
	private int totalData;
	private int totalInstr;
	private boolean unifiedHit;
	private boolean dataHit;
	private boolean instrHit;
	private DecimalFormat df4 = new DecimalFormat("#.####");

	/**
	 * Default constructor.
	 * 
	 * @param c: Array of caches to run the simulation on.
	 */
	public CacheSim(Cache[] c) {
		cacheArray = c;
		unifiedMisses = 0;
		dataMisses = 0;
		instrMisses = 0;
		totalData = 0;
		totalInstr = 0;
		unifiedHit = false;
		dataHit = false;
		instrHit = false;
	}

	/**
	 * Simulates addresses being sent to the cache, handles misses for both unified
	 * and split.
	 * 
	 * @param addresses List of addresses to run through the cache.
	 * @return Results of the simulation.
	 */
	public String simulate(ArrayList<Address> addresses) {
		unifiedHit = false;
		dataHit = false;
		instrHit = false;
		for (int i = 0; i < addresses.size(); i++) {
			switch (addresses.get(i).getId()) {
			case 0:
			case 1:
				if (cacheArray.length == 1) {
					unifiedHit = cacheArray[0].request(addresses.get(i));
					if (unifiedHit == false) {
						unifiedMisses++;
					}
				} else {
					totalData++;
					dataHit = cacheArray[1].request(addresses.get(i));
					if (dataHit == false) {
						dataMisses++;
					}
				}
				break;
			case 2:
				if (cacheArray.length == 1) {
					unifiedHit = cacheArray[0].request(addresses.get(i));
					if (unifiedHit == false) {
						unifiedMisses++;
					}
				} else {
					totalInstr++;
					instrHit = cacheArray[0].request(addresses.get(i));
					if (instrHit == false) {
						instrMisses++;
					}
				}
				break;
			}
		}

		// If array length is one, return unified cache results.
		if (cacheArray.length == 1) {
			double missDouble = unifiedMisses;
			return "\n[RESULTS]\nUnified Cache\nDemand Fetches: " + addresses.size() + "\nDemand Misses: "
					+ unifiedMisses + "\nDemand Miss Rate: " + df4.format(missDouble / addresses.size());
		}

		// Return split cache results.
		double missInstrDouble = instrMisses;
		double missDataDouble = dataMisses;
		return "\n[RESULTS]\nSplit Cache\nInstruction\n---------------\nDemand Fetches: " + totalInstr
				+ "\nDemand Misses: " + instrMisses + "\nDemand Miss Rate: " + df4.format(missInstrDouble / totalInstr)
				+ "\n\nData\n---------------\nDemand Fetches: " + totalData + "\nDemand Misses: " + dataMisses
				+ "\nDemand Miss Rate: " + df4.format(missDataDouble / totalData);
	}

}
