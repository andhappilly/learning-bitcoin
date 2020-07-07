package ch1;

import static bitcoin.util.BigInt.from;

import java.math.BigInteger;

import bitcoin.math.field.finite.Element;

public final class Exercises {
	private Exercises() {}
	
	public static void main(String[] args) {
		// Exercise 2
		System.out.println("***** Exercise 2 *****");
		BigInteger order = from(57);
		
		Element e1 = new Element(from(44), order);
		Element e2 = new Element(from(33), order);		
		System.out.println("44 + 33 = "+e1.add(e2));
		
		e1 = new Element(from(9), order);
		e2 = new Element(from(29), order);
		System.out.println("9 - 29 = "+e1.subtract(e2));
		
		e1 = new Element(from(17), order);
		e2 = new Element(from(42), order);
		Element e3 = new Element(from(49), order);
		System.out.println("17 + 42 + 49 = "+e1.add(e2).add(e3));
		
		e1 = new Element(from(52), order);
		e2 = new Element(from(30), order);
		e3 = new Element(from(38), order);
		System.out.println("52 – 30 – 38 = "+e1.subtract(e2).subtract(e3));
		System.out.println();
		
		// Exercise 4	
		System.out.println("***** Exercise 4 *****");
		order = from(97);
		e1 = new Element(from(95), order);
		e2 = new Element(from(45), order);
		e3 = new Element(from(31), order);
		System.out.println("95 * 45 * 31 = "+e1.multiply(e2).multiply(e3));
		
		e1 = new Element(from(17), order);
		e2 = new Element(from(13), order);
		e3 = new Element(from(19), order);
		Element e4 = new Element(from(44), order);
		System.out.println("17 * 13 * 19 * 44 = "+e1.multiply(e2).multiply(e3).multiply(e4));
		
		e1 = new Element(from(12), order);
		e2 = new Element(from(77), order);
		System.out.println("(12 ^ 7) * (77 ^ 49) = "
					+e1.power(from(7)).multiply(e2.power(from(49))));// wrong!!! use big integer
		System.out.println();
		
		// Exercise 5 ...
		System.out.println("***** Exercise 5 *****");
		order = from(19);
		int[] multiples = new int[] {1, 3, 7, 13, 18}; 
		for (int k: multiples) { 
			Element m = new Element(from(k), order);
			System.out.print("k="+k+": {");
			String separator = "";
			for (int i = 0; i < 19; ++i) {
				System.out.print(separator);
				Element f = new Element(from(i), order);
				System.out.print(m.multiply(f));
				separator = ", ";
			} 
			System.out.println("}");
		}		 
		System.out.println();
		
		// Exercise 7
		System.out.println("***** Exercise 7 *****");
		int[] primes = new int[] {7, 11, 17, 31};
		for (int p : primes) {
			order = from(p);
			int orderMinusOne = p - 1;
			System.out.print("p="+p+": {");
			String separator = "";
			for (int i = 1; i < p; ++i) {
				System.out.print(separator);
				Element f = new Element(from(i), order);
				System.out.print(f.power(from(orderMinusOne)));
				separator = ", ";
			}
			System.out.println("}");
		}
		System.out.println();
		
		// Exercise 8
		System.out.println("***** Exercise 7 *****");
		order = from(31);
		e1 = new Element(from(3), order);
		e2 = new Element(from(24), order);
		System.out.println("3/24 = "+e1.divide(e2));
		
		e1 = new Element(from(17), order);
		System.out.println("17^-3 = "+e1.power(from(-3)));
		
		e1 = new Element(from(4), order);
		e2 = new Element(from(11), order);
		System.out.println("4^-4 * 11 = "+e1.power(from(-4)).multiply(e2));
		System.out.println();
	}
}
