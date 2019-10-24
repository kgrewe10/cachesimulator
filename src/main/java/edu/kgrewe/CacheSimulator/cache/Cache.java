package edu.kgrewe.CacheSimulator.cache;

import java.util.ArrayList;
import java.util.List;

import edu.kgrewe.CacheSimulator.address.Address;
import edu.kgrewe.CacheSimulator.utility.Utility;

/**
 * Represents a physical cache memory.
 * 
 * @author kgrewe
 *
 */
public class Cache {
	private int totalsize;
	private int blocksize;
	private int assoc;
	private int indexBits;
	private int offsetBits;
	private int tagBits;
	private int numBlocks;
	private Block[] ways[];
	private int[] lrubits[];
	private int lruMax;

	public Cache(int ts, int bs, int a) {
		setTotalsize(ts);
		setBlocksize(bs);
		setAssoc(a);
		indexBits = 0;
		offsetBits = 0;
		tagBits = 0;
		numBlocks = 0;
		ways = null;
		lrubits = null;
		lruMax = 0;
		setUpCache();
	}

	public void setUpCache() {
		// Calculate number of blocks and the index bits.
		numBlocks = totalsize / blocksize;
		indexBits = Utility.getPower(numBlocks);

		// If fully associative, set the index bits to 0.
		if (assoc == numBlocks) {
			indexBits = 0;
		}

		// Calculate all bits.
		offsetBits = Utility.getPower(blocksize);
		indexBits = Utility.getPower(assoc);
		tagBits = 64 - indexBits - offsetBits;

		// Subtract 1 from the index for every power of 2 associativity and increase the
		// tag bits.
		if (assoc > 1) {
			for (int i = 0; i < Utility.getPower(assoc); i++) {
				indexBits--;
				tagBits++;
				numBlocks /= 2;
			}
		}

		// Structure the cache based on associativity.
		if (assoc == 1) {
			// Create 1 way cache blocks.
			ways = new Block[1][numBlocks];
			for (int i = 0; i < numBlocks; i++) {
				ways[0][i] = new Block(-1);
			}
		} else {
			// Create the blocks for associativity.
			ways = new Block[assoc][numBlocks];
			for (int j = 0; j < assoc; j++) {
				for (int k = 0; k < numBlocks; k++) {
					ways[j][k] = new Block(-1);
				}
			}
			// Initialize the lru max and bits.
			lruMax = assoc - 1;
			lrubits = new int[assoc][numBlocks];

			// Create an array of lru bits for each set and initialize the array to the max
			// lru bits value.
			for (int j = 0; j < assoc; j++) {
				for (int k = 0; k < numBlocks; k++) {
					lrubits[j][k] = lruMax;
				}
			}
		}

	}

	/**
	 * Returns true on cache hit, false on cache miss.
	 * 
	 * @param addr The address of the fetch.
	 * @return cache hit - t or f
	 */
	public boolean request(Address addr) {
		// Get the address.
		String addy = addr.getAddress();
		// System.out.println("Address " + addy);
		StringBuilder bin = new StringBuilder(Utility.hexToBin(addy));
		// System.out.println("Binary " + bin);

		if (bin.length() < 64) {
			int add = 64 - bin.length();
			for (int i = 0; i < add; i++) {
				bin.insert(0, "0");
			}
		}

		// Parse the tag from the address.
		long tag = 0;
		try {
			tag = Long.parseLong(Utility.BinToDec(bin.substring(0, 0 + tagBits)));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}

		// System.out.println("Tag " + tag);

		int modulus = 0;

		// Determine if a request is a hit or miss.
		switch (assoc) {
		case 1:
			// Direct Mapped.
			// Calculate the modulus.
			modulus = (int) (tag % numBlocks);
			if (tag == ways[0][modulus].getTag()) {
				return true;
			} else {
				ways[0][modulus] = new Block(tag);
				return false;
			}
		case 2:
		case 4:
		case 8:
		case 16:
		case 32:
		case 64:
		case 128:
			// Set Associative.
			// Calculate the modulus.
			modulus = (int) (tag % numBlocks);

			// Iterate through all blocks in the set.
			for (int i = 0; i < assoc; i++) {
				// If the tag is a match, its a hit.
				if (tag == ways[i][modulus].getTag()) {
					// Get all the LRU bits for a set to increment them.listSet.
					int set[] = new int[assoc];
					for (int k = 0; k < assoc; k++) {
						set[k] = lrubits[k][modulus];
					}
					set[i] = -1;

					// Set the lru bits of the hit block to 0.
					lrubits[i][modulus] = 0;

					// Use a list for index of operations.
					List<Integer> listSet = new ArrayList<Integer>();
					for (int in : set) {
						listSet.add(in);
					}

					// Iterate all necessary lru bits.
					for (int k = assoc - 2; k > -1; k--) {
						int index = listSet.indexOf(k);
						if (index > -1) {
							if ((k + 1) <= lruMax && listSet.indexOf(k + 1) < 0) {
								lrubits[index][modulus]++;
								listSet.set(index, listSet.get(index) + 1);
							}
						} else {
							continue;
						}
					}
					listSet.set(i, 0);
					return true;
				}
			}

			// LRU Replacement if not found in the set.
			replaceLRU(tag, modulus);
			return false;
		}
		return false;
	}

	/**
	 * Replaces the LRU for associative caches.
	 * 
	 * @param tag     The tag to add to the cache.
	 * @param modulus The set to check lru bits.
	 */
	public void replaceLRU(long tag, int modulus) {
		// Get all the LRU bits for a set to determine LRU.
		int set[] = new int[assoc];
		for (int i = 0; i < assoc; i++) {
			set[i] = lrubits[i][modulus];
		}

		// Replace the LRU with the new block, reset the LRU bits.
		int replace = 0;
		for (int i = 0; i < assoc; i++) {
			if (set[i] == lruMax) {
				replace = i;
				set[i] = -1;
				break;
			}
		}

		// Get the new block of the specified tag from memory.
		ways[replace][modulus] = new Block(tag);

		// Set LRU bits of the replaced block to 0.
		lrubits[replace][modulus] = 0;

		// Get a list to use indexOf operations.
		List<Integer> listSet = new ArrayList<Integer>();
		for (int in : set) {
			listSet.add(in);
		}

		// Iterate all necessary lru bits.
		for (int k = assoc - 2; k > -1; k--) {
			int index = listSet.indexOf(k);
			if (index > -1) {
				if ((k + 1) <= lruMax && listSet.indexOf(k + 1) < 0) {
					lrubits[index][modulus]++;
					listSet.set(index, listSet.get(index) + 1);
				}
			} else {
				continue;
			}
		}
		listSet.set(replace, 0);
	}

	public int getBlocksize() {
		return blocksize;
	}

	public void setBlocksize(int blocksize) {
		this.blocksize = blocksize;
	}

	public int getTotalsize() {
		return totalsize;
	}

	public void setTotalsize(int totalsize) {
		this.totalsize = totalsize;
	}

	public int getAssoc() {
		return assoc;
	}

	public void setAssoc(int assoc) {
		this.assoc = assoc;
	}
}
