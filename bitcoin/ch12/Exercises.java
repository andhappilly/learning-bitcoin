package ch12;

import static bitcoin.util.Bytes.bytesToHex;
import static bitcoin.util.Crypto.hash160;

import java.math.BigInteger;
import java.util.Arrays;

import bitcoin.util.BloomFilter;

public final class Exercises {
	private Exercises() {}
	
	public static void main(String[] args) {
		System.out.println("***** Exercise 1 *****");
		int[] filter = new int[10];
		Arrays.fill(filter, 0);
		
		String data = "hello world";
		byte[] hash = hash160(data.getBytes());
		int bucket = new BigInteger(1, hash).remainder(BigInteger.TEN).intValue();
		filter[bucket] = 1;
		
		data = "goodbye";
		hash = hash160(data.getBytes());
		bucket = new BigInteger(1, hash).remainder(BigInteger.TEN).intValue();
		filter[bucket] = 1;
		
		System.out.print("[ ");
		for(int i: filter) {
			System.out.print(i);
			System.out.print(" ");
		}
		System.out.println("]");
		
		System.out.println();
		
		System.out.println("***** Exercise 2 *****");
		
		BloomFilter bFilter = new BloomFilter(10, BigInteger.valueOf(5), BigInteger.valueOf(99));
		bFilter.add("Hello World".getBytes());
		bFilter.add("Goodbye".getBytes());
		
		byte[] bitFields = bFilter.getBitsField();
		
		System.out.println(bytesToHex(bitFields));
	}
}
