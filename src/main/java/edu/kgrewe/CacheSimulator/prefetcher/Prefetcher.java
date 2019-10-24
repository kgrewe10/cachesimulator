package edu.kgrewe.CacheSimulator.prefetcher;

import java.util.ArrayList;
import java.util.List;

import edu.kgrewe.CacheSimulator.address.Address;
import edu.kgrewe.CacheSimulator.cache.Block;
import edu.kgrewe.CacheSimulator.utility.Utility;

public class Prefetcher {
	List<Block> fetchedBlocks;
	List<ArrayList<Long>> table;
	long maxConfidence;
	long bufferSize;
	long numBlocks;
	long prefetchBits;
	long timesNotUsedDecrease;
	String onePrev;
	String twoPrev;

	public Prefetcher(long bs, long pb, long cacheSize) {
		// Validate the prefetcher bits.
		if (pb < 2 || pb > 3) {
			System.out.println("Prefetch bits out of range.  Must be 2 or 3.  Rerun and try again.");
			System.exit(0);
		}

		// Validate the buffer size.
		if (bs % 2 != 0) {
			System.out.println("Prefetcher buffer size not a multiple of 2.  Rerun and try again.");
			System.exit(0);
		}

		// Assign all needed data.
		bufferSize = bs;
		prefetchBits = pb;
		numBlocks = bufferSize / cacheSize;
		timesNotUsedDecrease = 10;
		maxConfidence = Utility.getPower(Integer.parseInt(Long.toString(prefetchBits)));
		fetchedBlocks = new ArrayList<Block>();
		table = new ArrayList<ArrayList<Long>>();
		onePrev = "";
		twoPrev = "";

		// Add the number of blocks the prefetcher can hold to the array.
		for (int i = 0; i < numBlocks; i++) {
			fetchedBlocks.add(new Block(-1));
		}

		// Create a pattern table.
		for (int i = 0; i < numBlocks; i++) {
			for (int j = 0; j < 5; j++) {
				table.get(i).add((long) -1);
			}
		}
	}

	/**
	 * Monitors the lowest level of cache, for this program it monitors L1.
	 * 
	 * @param addr   The address requested from the cache.
	 * @param result Cache hit or miss.
	 */
	public void monitor(Address addr, boolean result) {
		String current = addr.getAddress();
		// System.out.println("Address " + current);

		// Remove LSBs.
		current = current.substring(0, current.length() - 3);

		// System.out.println("Address removed 3 LSB" + current);
		StringBuilder bin = new StringBuilder(Utility.hexToBin(current));
		// System.out.println("Binary " + bin);

		if (bin.length() < 64) {
			int add = 64 - bin.length();
			for (int i = 0; i < add; i++) {
				bin.insert(0, "0");
			}
		}

		// Change the address to decimal.
		long tag = 0;
		try {
			tag = Long.parseLong(Utility.BinToDec(bin.toString()));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		// Check to see if there was a hit on a stride in the pattern table.
		for (int i = 0; i < numBlocks; i++) {
			if (tag == table.get(i).get(3)) {

			}
		}

	}

	/**
	 * Simulates a memory request being sent to the prefetcher.
	 * 
	 * @return true=hit, false=miss.
	 */
	public boolean request(Address addr) {
		String current = addr.getAddress();
		// System.out.println("Address " + current);

		// Remove LSBs.
		current = current.substring(0, current.length() - 3);

		// System.out.println("Address removed 3 LSB" + current);
		StringBuilder bin = new StringBuilder(Utility.hexToBin(current));
		// System.out.println("Binary " + bin);

		if (bin.length() < 64) {
			int add = 64 - bin.length();
			for (int i = 0; i < add; i++) {
				bin.insert(0, "0");
			}
		}

		// Change the address to decimal.
		long tag = 0;
		try {
			tag = Long.parseLong(Utility.BinToDec(bin.toString()));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		for (int i = 0; i < numBlocks; i++) {
			if (tag == fetchedBlocks.get(i).getTag()) {
				return true;
			}
		}
		return false;
	}

}
