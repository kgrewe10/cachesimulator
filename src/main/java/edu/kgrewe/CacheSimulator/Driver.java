package edu.kgrewe.CacheSimulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import edu.kgrewe.CacheSimulator.address.Address;
import edu.kgrewe.CacheSimulator.address.DataRead;
import edu.kgrewe.CacheSimulator.address.DataWrite;
import edu.kgrewe.CacheSimulator.address.InstructionFetch;
import edu.kgrewe.CacheSimulator.cache.Cache;
import edu.kgrewe.CacheSimulator.cache.CacheSim;
import edu.kgrewe.CacheSimulator.prefetcher.StridePrefetcher;
import edu.kgrewe.CacheSimulator.utility.Utility;

/**
 * Drives the program.
 *
 */
public class Driver {
	public static void main(String[] args) {
		// If there a no arguments, throw an error.
		if (args.length == 0) {
			System.out.println("You must enter arguments.  Type in help as a parameter for more info.");
			System.exit(0);
		}

		// Declare variables to retrieve input for the program.
		String type = null;
		int totalsize = 0;
		int blocksize = 0;
		int assoc = 0;
		String filein = null;
		String fileout = null;
		int buffer = 0;
		int confidenceBits = 0;
		File out = null;
		PrintWriter printer = null;
		boolean prefetcher = false;

		// If the number of arguments is 1 or 5, do something.
		if (args.length == 8 || args.length == 6 || args.length == 1) {
			// Print out info if help is first argument.
			if (args[0].equals("help") || args.length == 1) {
				System.out.println("Cache Simulator\n\nHELP MENU");
				System.out.println("This application takes in 6 arguments in order as specified below.");
				System.out.println("LRU is the assumed replacement policy.");
				System.out.println(
						"If a split cache is specified, the entered blocksize and associativity will be used for both instruction and data caches with total cache size split equally between both.");
				System.out.println("\nARGUMENTS FORMAT\n");
				System.out.println(
						"[type] [cachesize] [blocksize] [associativity] [inputfilename] [outputfilename] OPTIONAL [prefetchbuffer] [confidencebits]\n");
				System.out.println("type: The type of cache - split or unified.");
				System.out.println("     values: s = split, u = unified");
				System.out.println("cachesize: The total cache size in bytes.");
				System.out.println("     values: any multiple of two");
				System.out.println("blocksize: The block size for each block in the cache in bytes.");
				System.out.println("     values: any multiple of two");
				System.out.println("associativity: The associativity of the cache.  Use 1 for direct mapped.");
				System.out.println("     values: any multiple of two");
				System.out.println("inputfilename: The name of the input file to use for the simulation.");
				System.out.println("     values: a string");
				System.out.println("outputfilename: The name of the output file to create with results.");
				System.out.println("     values: a string\n");
				System.out.println("-----------------------------------------");
				System.out.println("OPTIONAL ARGUMENTS");
				System.out.println("-----------------------------------------");
				System.out.println("\nprefetchbuffer: The size of the prefetcher memory.");
				System.out.println("     values: any multiple of two");
				System.out.println("confidencebits: The number of bits to use to determine confidence.");
				System.out.println("     values: 2 or 3, default is 3");
				System.exit(0);
			}

			// Input validation.
			try {
				type = args[0];
				totalsize = Integer.parseInt(args[1]);
				blocksize = Integer.parseInt(args[2]);
				assoc = Integer.parseInt((args[3]));
				filein = args[4];
				fileout = args[5];
				if (args.length == 8) {
					buffer = Integer.parseInt(args[6]);
					confidenceBits = Integer.parseInt(args[7]);
					prefetcher = true;
				}
			} catch (Exception e) {
				System.out.println("Mismatched argument types.\nEnter help as the only argument to view the format.");
				System.exit(0);
			}

			if (!Utility.isPowerOfTwo(totalsize) || !Utility.isPowerOfTwo(blocksize)) {
				System.out.println("Number not a multiple of two.  Rerun and try again.");
				System.exit(0);
			}

			if (!Utility.isPowerOfTwo(assoc) || !Utility.isPowerOfTwo(buffer) && args.length == 8) {
				System.out.println("Number not a multiple of two.  Rerun and try again.");
				System.exit(0);
			}

			if ((confidenceBits < 2 || confidenceBits > 3) && args.length == 8) {
				System.out.println("Confidence bits out of range.  Must be 2 or 3.  Rerun and try again.");
				System.exit(0);
			}

			out = new File(fileout + ".txt");
			try {
				printer = new PrintWriter(out);
			} catch (FileNotFoundException e1) {
				System.out.println("Couldn't create PrintWriter");
				System.exit(0);
			}

			System.out.println("\n[SETUP]");
			System.out.println("Reading input...");
			// Prints out the values entered to the user.
			System.out.println("Cache Type: " + type);
			System.out.println("Cache Size: " + totalsize);
			System.out.println("Block Size: " + blocksize);
			System.out.println("Associativity: " + assoc);
			if (prefetcher) {
				System.out.println("Prefetch Buffer: " + buffer);
				System.out.println("Confidence Bits: " + confidenceBits);
			}
			System.out.println("Input Filename: " + filein);
			System.out.println("Output Filename: " + fileout + ".txt");

			printer.println("Summary of options (help as only argument for more info)\n");
			printer.println("Cache Type: " + type);
			printer.println("Cache Size: " + totalsize);
			printer.println("Block Size: " + blocksize);
			printer.println("Associativity: " + assoc);
			if (prefetcher) {
				printer.println("Prefetch Buffer: " + buffer);
				printer.println("Confidence Bits: " + confidenceBits);
			}
			printer.println("Input Filename: " + filein);
			printer.println("Output Filename: " + fileout + ".txt");
			printer.println("\n");

			System.out.println("Parsing input file...");
			// Read input file and parse it.
			Scanner input = null;
			ArrayList<Address> addresses = new ArrayList<Address>();
			int i = 0;
			try {
				input = new Scanner(new File(filein));
				while (input.hasNext()) {
					i++;
					if (i > 4000000) {
						break;
					}
					String arr[] = input.nextLine().split(" ");
					if (arr[1].contains("0x")) {
						arr[1] = arr[1].replace("0x", "");
					}
					switch (Integer.parseInt(arr[0])) {
					case 0:
						addresses.add(new DataRead(arr[1]));
						break;
					case 1:
						addresses.add(new DataWrite(arr[1]));
						break;
					case 2:
						addresses.add(new InstructionFetch(arr[1]));
						break;
					}
				}
				input.close();
			} catch (Exception e) {
				System.out.println("Error reading input file.  Check path and rerun the application.");
				System.exit(0);
			}

			System.out.println("Building cache...");
			CacheSim cc = null;
			// Determine the type of cache to create.
			if (type.equals("s")) {
				int splitsize = totalsize / 2;
				System.out.println("Creating " + (splitsize / 1000) + " KB instruction cache...");
				Cache c = new Cache(splitsize, blocksize, assoc);
				System.out.println("Creating " + (splitsize / 1000) + " KB data cache...");
				Cache c2 = new Cache(splitsize, blocksize, assoc);
				Cache split[] = { c, c2 };
				cc = new CacheSim(split);
				if (prefetcher) {
					System.out.println("Building prefetcher...");
					System.out.println("Creating " + buffer + " buffer size prefetcher...");
					StridePrefetcher p = new StridePrefetcher(buffer, confidenceBits, blocksize);
					cc = new CacheSim(split, p);
				}
			} else if (type.equals("u")) {
				System.out.println("Creating unified " + (totalsize / 1000) + " KB cache...");
				Cache c = new Cache(totalsize, blocksize, assoc);
				Cache unified[] = { c };
				cc = new CacheSim(unified);
				if (prefetcher) {
					System.out.println("Building prefetcher...");
					System.out.println("Creating " + buffer + " buffer size prefetcher...");
					StridePrefetcher p = new StridePrefetcher(buffer, confidenceBits, blocksize);
					cc = new CacheSim(unified, p);
				}
			} else {
				System.out.println("Invalid argument for type. Rerun and try again.");
				System.exit(0);
			}

			System.out.println("Setup complete.");

			// Starts the simulation.
			System.out.println("\n[SIMULATE]");
			System.out.println("Starting cache simulation...");
			String results = cc.simulate(addresses);
			System.out.println("Cache simulation completed.");
			System.out.println(results);

			printer.println(results);

		} else {
			System.out.println("Invalid number of arguments.  Rerun and try again.");
			System.exit(0);
		}
		printer.close();

	}
}
