package edu.kgrewe.CacheSimulator.cache;

import java.text.DecimalFormat;
import java.util.ArrayList;

import edu.kgrewe.CacheSimulator.address.Address;
import edu.kgrewe.CacheSimulator.prefetcher.StridePrefetcher;

/**
 * Runs a cache hit/miss simulation.
 * 
 * @author kgrewe
 *
 */
public class CacheSim {
	private Cache cacheArray[];
	private StridePrefetcher prefetcher;
	private int unifiedMisses;
	private int dataMisses;
	private int instrMisses;
	private int prefetchMisses;
	private int totalData;
	private int totalInstr;
	private int totalPrefetch;
	private boolean unifiedHit;
	private boolean dataHit;
	private boolean instrHit;
	private boolean prefetchHit;
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
		prefetchMisses = 0;
		totalData = 0;
		totalInstr = 0;
		totalPrefetch = 0;
		unifiedHit = false;
		dataHit = false;
		instrHit = false;
		prefetchHit = false;
		prefetcher = null;
	}

	/**
	 * Overloaded constructor.
	 * 
	 * @param c Array of caches.
	 * @param p Prefetcher associated with the caches.
	 */
	public CacheSim(Cache[] c, StridePrefetcher p) {
		cacheArray = c;
		unifiedMisses = 0;
		dataMisses = 0;
		instrMisses = 0;
		totalData = 0;
		totalInstr = 0;
		unifiedHit = false;
		dataHit = false;
		instrHit = false;
		prefetcher = p;
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
		boolean pre = (prefetcher != null);

		for (int i = 0; i < addresses.size(); i++) {
			Address addr = addresses.get(i);
			switch (addresses.get(i).getId()) {
			case 0:
			case 1:
				// Unified cache.
				if (cacheArray.length == 1) {
					// Check for hit.
					unifiedHit = cacheArray[0].request(addr);
					// If its a miss, increase misses.
					if (unifiedHit == false) {
						unifiedMisses++;

						// If prefetcher is enabled, send a request and determine hit or miss.
						if (pre) {
							totalPrefetch++;
							prefetchHit = prefetcher.request(addr);
							if (prefetchHit == false) {
								prefetchMisses++;
							}
							prefetcher.monitor(addr, unifiedHit);
						}
					}

				} else {
					// Split data cache.
					totalData++;

					// Check for a hit.
					dataHit = cacheArray[1].request(addr);

					// If its a miss, increase misses.
					if (dataHit == false) {
						dataMisses++;

						// If prefetcher is enabled, send a request and determine hit or miss.
						if (pre) {
							totalPrefetch++;
							prefetchHit = prefetcher.request(addr);
							if (prefetchHit == false) {
								prefetchMisses++;
							}
							prefetcher.monitor(addr, dataHit);
						}
					}
				}
				break;
			case 2:
				// Unified cache.
				if (cacheArray.length == 1) {
					unifiedHit = cacheArray[0].request(addr);
					if (unifiedHit == false) {
						unifiedMisses++;

						// If prefetcher is enabled, send a request and determine hit or miss.
						if (pre) {
							totalPrefetch++;
							prefetchHit = prefetcher.request(addr);
							if (prefetchHit == false) {
								prefetchMisses++;
							}
							prefetcher.monitor(addr, unifiedHit);
						}
					}

				} else {
					// Split instruction cache.
					totalInstr++;
					instrHit = cacheArray[0].request(addr);
					if (instrHit == false) {
						instrMisses++;

						// If prefetcher is enabled, send a request and determine hit or miss.
						if (pre) {
							totalPrefetch++;
							prefetchHit = prefetcher.request(addr);
							if (prefetchHit == false) {
								prefetchMisses++;
							}
							prefetcher.monitor(addr, instrHit);
						}
					}

				}
				break;
			}
		}

		String message = "";
		double missPrefetchDouble = prefetchMisses;
		// If array length is one, return unified cache results.
		if (cacheArray.length == 1) {
			double missDouble = unifiedMisses;
			message = "\n[CACHE RESULTS]\nUnified Cache\nDemand Fetches: " + addresses.size() + "\nDemand Misses: "
					+ unifiedMisses + "\nDemand Miss Rate: " + df4.format(missDouble / addresses.size());
			if (pre) {
				message = message.concat("\n\n[PREFETCH RESULTS]\nCache Misses: " + totalPrefetch
						+ "\nPrefetcher Misses: " + prefetchMisses + "\nPrefetch Miss Rate: "
						+ df4.format(missPrefetchDouble / totalPrefetch) + "\nCoverage: "
						+ df4.format((totalPrefetch - prefetchMisses) / (missDouble)) + "\n\nTotal Cache Miss Rate: "
						+ df4.format(((missDouble - (totalPrefetch - prefetchMisses)) / addresses.size())));
			}
			return message;
		}

		// Return split cache results.
		double missInstrDouble = instrMisses;
		double missDataDouble = dataMisses;

		message = "\n[CACHE RESULTS]\nSplit Cache\nInstruction\n---------------\nDemand Fetches: " + totalInstr
				+ "\nDemand Misses: " + instrMisses + "\nDemand Miss Rate: " + df4.format(missInstrDouble / totalInstr)
				+ "\n\nData\n---------------\nDemand Fetches: " + totalData + "\nDemand Misses: " + dataMisses
				+ "\nDemand Miss Rate: " + df4.format(missDataDouble / totalData);
		if (pre) {
			message = message.concat("\n\n[PREFETCH RESULTS]\nCache Misses: " + totalPrefetch + "\nPrefetcher Misses: "
					+ prefetchMisses + "\nPrefetch Miss Rate: " + df4.format(missPrefetchDouble / totalPrefetch)
					+ "\nCoverage: " + df4.format((totalPrefetch - prefetchMisses) / (missInstrDouble + missDataDouble))
					+ "\n\nTotal Cache Miss Rate: "
					+ df4.format((((missInstrDouble + missDataDouble) - (totalPrefetch - prefetchMisses))
							/ addresses.size())));
		}
		return message;
	}

}
