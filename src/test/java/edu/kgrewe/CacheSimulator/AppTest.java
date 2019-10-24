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
		System.out.println(getPower(5));
	}
	
	public static int getPower(int num) {
		int arr[] = {0, 1, 5};
		return Utility.findIndex(arr, num);
	}
}