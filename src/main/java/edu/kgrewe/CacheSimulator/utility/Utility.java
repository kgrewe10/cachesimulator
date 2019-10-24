package edu.kgrewe.CacheSimulator.utility;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Holds utility methods.
 * 
 * @author kgrewe
 *
 */
public class Utility {
	public static int getPower(int num) {
		int duplicate = num;
		int count = 0;
		while (duplicate >= 2) {
			duplicate = duplicate / 2;
			count++;
		}

		return count;
	}

	public static String hexToBin(String s) {
		return new BigInteger(s, 16).toString(2);
	}

	public static String BinToDec(String s) {
		return new BigInteger(s, 2).toString(10);
	}

	public static int findIndex(int arr[], int t) {
		int index = Arrays.binarySearch(arr, t);
		return (index < 0) ? -1 : index;
	}
}
