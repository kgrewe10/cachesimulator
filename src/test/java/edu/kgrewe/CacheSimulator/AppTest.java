package edu.kgrewe.CacheSimulator;

import java.math.BigInteger;

import edu.kgrewe.CacheSimulator.utility.Utility;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest {
	public static void main(String args[]) {
		System.out.println(isPowerOfTwo(0));
	}
	
	public static boolean isPowerOfTwo(int n) {
		if (n <= 0)
			return false;

		while (n > 2) {
			int t = n >> 1;
			int c = t << 1;

			if (n - c != 0)
				return false;

			n = n >> 1;
		}

		return true;
	}
}