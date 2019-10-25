package edu.kgrewe.CacheSimulator.prefetcher;

import java.util.ArrayList;
import java.util.List;

import edu.kgrewe.CacheSimulator.address.Address;
import edu.kgrewe.CacheSimulator.cache.Block;
import edu.kgrewe.CacheSimulator.utility.Utility;

/**
 * Simulates a stride prefetcher.
 * 
 * @author kgrewe
 *
 */
public class StridePrefetcher {
	private List<Block> fetchedBlocks;
	private List<Long> lruBits;
	private List<ArrayList<Long>> table;
	private long maxConfidence;
	private long bufferSize;
	private long numBlocks;
	private long prefetchBits;
	private long prefetchBaseline;
	static final long TIMES_TO_DECREASE = 100000;
	static final int NUM_LSB = 3;
	private String currentAddr;
	private long onePrev;
	private long twoPrev;

	/**
	 * Constructor.
	 * 
	 * @param bSize     Buffer size.
	 * @param pbits     Prefetch bits.
	 * @param blockSize Cache block size.
	 */
	public StridePrefetcher(long bSize, long pbits, long blockSize) {
		// Assign all needed data.
		bufferSize = bSize;
		prefetchBits = pbits;

		// Determine what confidence to prefetch at.
		if (pbits == 2) {
			prefetchBaseline = 2;
		} else {
			prefetchBaseline = 4;
		}

		numBlocks = bufferSize / blockSize;
		maxConfidence = (long) Math.pow(2, prefetchBits);
		fetchedBlocks = new ArrayList<Block>();
		lruBits = new ArrayList<Long>();
		table = new ArrayList<ArrayList<Long>>();
		onePrev = -1;
		twoPrev = -1;
		currentAddr = "-1";

		// Add the number of blocks the prefetcher can hold to the array.
		// Also create the LRU bits for replacment.
		for (int i = 0; i < numBlocks; i++) {
			fetchedBlocks.add(new Block(-1));
			lruBits.add(numBlocks);
			table.add(new ArrayList<Long>());
		}

		// Create a pattern table.
		for (int i = 0; i < numBlocks; i++) {
			for (int j = 0; j < 5; j++) {
				if (j == 3 || j == 4) {
					table.get(i).add((long) 0);
				} else {
					table.get(i).add((long) -1);
				}
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
		// Keep history of two previous cache requests.
		twoPrev = onePrev;
		onePrev = Long.parseLong(Utility.hexToDec(currentAddr));
		currentAddr = addr.getAddress();

		// System.out.println("Address " + current);

		// Remove LSBs.
		currentAddr = currentAddr.substring(0, currentAddr.length() - NUM_LSB);

		// System.out.println("Address removed 3 LSB" + current);
		StringBuilder bin = new StringBuilder(Utility.hexToBin(currentAddr));
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

		// If no next addresses match in the table, find strides relative to two
		// previous addresses.
		long strideOne = 0;
		long strideTwo = 0;

		// Calculate the first and second strides.
		strideOne = Math.abs(tag - onePrev);
		strideTwo = Math.abs(tag - twoPrev);

		if (onePrev == -1) {
			strideOne = 0;
		}
		if (twoPrev == -1) {
			strideTwo = 0;
		}

//		System.out.println("[STRIDES]");
//		System.out.println("Address: " + tag);
//		System.out.println("One previous: " + onePrev);
//		System.out.println("Two previous: " + twoPrev);
//		System.out.println("Stride One: " + strideOne);
//		System.out.println("Stride Two: " + strideTwo);

		// Increase all of the times not used.
		for (int i = 0; i < numBlocks; i++) {
			if (table.get(i).get(0) != -1) {
				table.get(i).set(4, table.get(i).get(4) + 1);
			}
		}

		// Check to see if there was a hit on a stride in the pattern table.
		for (int i = 0; i < numBlocks; i++) {
			// Next address was found in the table. Update the confidence if not equal to
			// max.
			if (tag == table.get(i).get(2)) {
				// System.out.println("Stride hit on addr: " + table.get(i).get(2));
				long confidence = table.get(i).get(3);
				// If less than max confidence, increase the confidence.
				if (confidence < maxConfidence) {
					table.get(i).set(3, confidence + 1);
					table.get(i).set(4, (long) 0);
					// System.out.println("Confidence increased.");
					// printTable();
				}

				// Set the new address to the current address in the table.
				table.get(i).set(0, tag);

				// Set the next address to the current address plus the stride.
				table.get(i).set(2, table.get(i).get(1) + tag);

				// Set the times since last use to 0.
				table.get(i).set(4, (long) 0);
				checkPrefetch();
				return;
			}
		}

		// Check to see if any address stride confidence needs to be decreased based on
		// the constant times to decrease.
		for (int i = 0; i < numBlocks; i++) {
			// If an stride hasn't been used in x requests, decrease the confidence by 1.
			if (table.get(i).get(4) >= TIMES_TO_DECREASE && table.get(i).get(3) != 0) {
				table.get(i).set(3, table.get(i).get(3) - 1);
			}
		}

		boolean first = true;

		// Check for any entries with that haven't been used.
		for (int i = 0; i < numBlocks; i++) {
			// If the address is in the table and the confidence of that entry is 0, replace
			// it with the new stride.
			if (table.get(i).get(0) == -1) {
				// Set address.
				table.get(i).set(0, tag);
				// System.out.println("Table entry being replaced with different tag: " + tag);

				// If its the first time finding a replacment, use first stride.
				// Else, use second stride.
				if (first) {
					// Set the stride.
					table.get(i).set(1, strideOne);
					// System.out.println("Stride one added to pattern table " + strideOne);

					// Set the next address.
					table.get(i).set(2, tag + strideOne);
					// System.out.println("Next address added to table: " + (tag + strideOne));
					// printTable();
					if (strideOne == strideTwo) {
						table.get(i).set(3, table.get(i).get(3) + 1);
						checkPrefetch();
						return;
					}
					first = false;
				} else {
					// Set the stride.
					table.get(i).set(1, strideTwo);
					// System.out.println("Stride two added to pattern table " + strideTwo);

					// Set the next address.
					table.get(i).set(2, tag + strideTwo);

					// System.out.println("Next address added to table: " + (tag + strideTwo));
					// printTable();
					checkPrefetch();
					return;
				}
			}
		}

		// Check for entries with the same address with confidence 0 to replace first.
		for (int i = 0; i < numBlocks; i++) {
			// If the address is in the table and the confidence of that entry is 0, replace
			// it with the new stride.
			if (tag == table.get(i).get(0) && table.get(i).get(3) == 0) {
				// System.out.println("Table entry being replaced with same tag: " + tag);

				// If its the first time finding a replacment, use first stride.
				// Else, use second stride.
				if (first) {
					// Set the stride.
					table.get(i).set(1, strideOne);
					// System.out.println("Stride one added to pattern table " + strideOne);

					// Set the next address.
					table.get(i).set(2, tag + strideOne);
					// System.out.println("Next address added to table: " + (tag + strideOne));
					// printTable();
					if (strideOne == strideTwo) {
						table.get(i).set(3, table.get(i).get(3) + 1);
						checkPrefetch();
						return;
					}
					first = false;
				} else {
					if (strideOne != strideTwo) {
						// Set the stride.
						table.get(i).set(1, strideTwo);
						// System.out.println("Stride two added to pattern table " + strideTwo);

						// Set the next address.
						table.get(i).set(2, tag + strideTwo);

						// System.out.println("Next address added to table: " + (tag + strideTwo));
						// printTable();
					}
					checkPrefetch();
					return;
				}
			}
		}

		// Check for any entries with confidence 0 to replace.
		for (int i = 0; i < numBlocks; i++) {
			// If the address is in the table and the confidence of that entry is 0, replace
			// it with the new stride.
			if (table.get(i).get(3) == 0) {
				// Set address.
				table.get(i).set(0, tag);
				// System.out.println("Table entry being replaced with different tag: " + tag);

				// If its the first time finding a replacment, use first stride.
				// Else, use second stride.
				if (first) {
					// Set the stride.
					table.get(i).set(1, strideOne);
					// System.out.println("Stride one added to pattern table " + strideOne);

					// Set the next address.
					table.get(i).set(2, tag + strideOne);
					// System.out.println("Next address added to table: " + (tag + strideOne));
					// printTable();
					if (strideOne == strideTwo) {
						table.get(i).set(3, table.get(i).get(3) + 1);
						checkPrefetch();
						return;
					}
					first = false;
				} else {
					if (strideOne != strideTwo) {
						// Set the stride.
						table.get(i).set(1, strideTwo);
						// System.out.println("Stride two added to pattern table " + strideTwo);

						// Set the next address.
						table.get(i).set(2, tag + strideTwo);

						// System.out.println("Next address added to table: " + (tag + strideTwo));
						// printTable();
					}
					checkPrefetch();
					return;
				}
			}
		}
	}

	/**
	 * Checks to see if an address in the table needs to be prefetched.
	 */
	private void checkPrefetch() {
		// Check for a prefetch.
		for (int i = 0; i < fetchedBlocks.size(); i++) {
			// If the confidence is equal or greater than prefetch baseline, prefetch.
			if (table.get(i).get(3) >= prefetchBaseline) {
				boolean inMem = false;
				for (int j = 0; j < numBlocks; j++) {
					if (fetchedBlocks.get(j).getTag() == table.get(i).get(2)) {
						inMem = true;
					}
				}

				if (!inMem) {
					System.out.println(
							"Prefetching addr: " + table.get(i).get(2) + " at confidence " + table.get(i).get(3));
					prefetch(table.get(i).get(2));
				}
			}
		}
//		System.out.println("-----------------");
//		System.out.println("Prefetched Blocks");
//
//		for (int i = 0; i < numBlocks; i++) {
//			if (fetchedBlocks.get(i).getTag() == -1) {
//				break;
//			}
//			System.out.println(fetchedBlocks.get(i).getTag());
//		}
//			System.out.println("-----------------");
	}

	public void printTable() {
		System.out.println("---------------------TablePrint-----------------");
		for (int i = 0; i < numBlocks; i++) {
			if (table.get(i).get(0) == -1) {
				break;
			}
			for (int j = 0; j < table.get(i).size(); j++) {
				System.out.print(table.get(i).get(j) + " ");
			}
			System.out.println("\n\n");
		}
	}

	/**
	 * Simulates a memory request being sent to the prefetcher.
	 * 
	 * @return true=hit, false=miss.
	 */
	public boolean request(Address addr) {
		String current = addr.getAddress();

		// Remove LSBs.
		current = current.substring(0, current.length() - NUM_LSB);

		// Change the address to decimal.
		String tagString = Utility.hexToDec(current);
		long tag = 0;
		try {
			tag = Long.parseLong(tagString);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		for (int i = 0; i < numBlocks; i++) {
			if (fetchedBlocks.get(i).getTag() == -1) {
				break;
			}
			if (tag == fetchedBlocks.get(i).getTag()) {
				// System.out.println("Hit");
				return true;
			}
		}
		return false;
	}

	/**
	 * Prefetches an address, uses LRU for replacement.
	 * 
	 * @param addr The address to prefetch.
	 */
	public void prefetch(long addr) {
//		System.out.print("Before");
//		for(int i = 0; i < numBlocks; i++) {
//			System.out.print(fetchedBlocks.get(i).getTag() + " ");
//		}
		// Find the block to replace.
		int replace = 0;
		for (int i = 0; i < fetchedBlocks.size(); i++) {
			if (lruBits.get(i) == numBlocks) {
				replace = i;
				break;
			}
		}

		// Replace the block and reset the lru bit of the replaced block to -1.
		fetchedBlocks.set(replace, new Block(addr));
		lruBits.set(replace, (long) -1);

		// Iterate all necessary lru bits.
		for (long i = numBlocks; i > -1; i--) {
			int index = fetchedBlocks.indexOf(i);
			if (index > -1) {
				if ((i + 1) <= numBlocks && fetchedBlocks.indexOf(i + 1) < 0) {
					lruBits.set(index, lruBits.get(index) + 1);
				}
			} else {
				continue;
			}
		}

		// Set the lru bits of the replaced block to 0
		lruBits.set(replace, (long) 0);
//		System.out.print("After");
//		for(int i = 0; i < numBlocks; i++) {
//			System.out.print(fetchedBlocks.get(i).getTag() + " ");
//		}
	}

}
