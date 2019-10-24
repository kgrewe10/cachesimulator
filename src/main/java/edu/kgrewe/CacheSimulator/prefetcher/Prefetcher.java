package edu.kgrewe.CacheSimulator.prefetcher;

import java.util.ArrayList;
import java.util.List;

import edu.kgrewe.CacheSimulator.address.Address;
import edu.kgrewe.CacheSimulator.cache.Block;
import edu.kgrewe.CacheSimulator.utility.Utility;

public class Prefetcher {
	List<Block> fetchedBlocks;
	List<List<Long>> table = new ArrayList<List<Long>>();
	long maxConfidence;
	long bufferSize;
	long numBlocks;
	long prefetchBits;
	long timesNotUsedDecrease;

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

		//Assign all needed data.
		bufferSize = bs;
		prefetchBits = pb;
		numBlocks = bufferSize / cacheSize;
		timesNotUsedDecrease = 10;
		maxConfidence = Utility.getPower(Integer.parseInt(Long.toString(prefetchBits)));

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
	
	public boolean request(Address addr) {
		return false;
	}

}
