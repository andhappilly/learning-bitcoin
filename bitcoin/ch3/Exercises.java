package ch3;

import static bitcoin.crypto.ecc.Secp256k1.G;
import static bitcoin.util.BigInt.from;

import java.math.BigInteger;

import bitcoin.crypto.ecc.Secp256k1Element;
import bitcoin.crypto.ecc.Secp256k1Point;
import bitcoin.crypto.ecc.Secret;
import bitcoin.crypto.ecc.Signature;
import bitcoin.math.ellipticcurve.Point;
import bitcoin.math.field.finite.Element;
import bitcoin.util.Crypto;

public final class Exercises {
	private Exercises() {}
	
	public static void main(String[] args) {
		// Exercise 1 
		System.out.println("***** Exercise 1 *****");
		BigInteger order = from(223);
		
		Element a = new Element(from(0), order);
		Element b = new Element(from(7), order);
		
		Element x = new Element(from(192), order);
		Element y = new Element(from(105), order);
		
		try {
			Point.fromCurve(x, y, a, b);
			System.out.println("Point (192,105) is on the curve.");
		} catch(Exception e) {
			System.out.println("Point (192,105) is NOT on the curve.");
		}
		
		x = new Element(from(17), order);
		y = new Element(from(56), order);
		
		try {
			Point.fromCurve(x, y, a, b);
			System.out.println("Point (17,56) is on the curve.");
		} catch(Exception e) {
			System.out.println("Point (17,56) is NOT on the curve.");
		}
		
		x = new Element(from(200), order);
		y = new Element(from(119), order);
		
		try {
			Point.fromCurve(x, y, a, b);
			System.out.println("Point (200,119) is on the curve.");
		} catch(Exception e) {
			System.out.println("Point (200,119) is NOT on the curve.");
		}
		
		x = new Element(from(1), order);
		y = new Element(from(193), order);
		
		try {
			Point.fromCurve(x, y, a, b);
			System.out.println("Point (1,193) is on the curve.");
		} catch(Exception e) {
			System.out.println("Point (1,193) is NOT on the curve.");
		}
		
		x = new Element(from(42), order);
		y = new Element(from(99), order);
		
		try {
			Point.fromCurve(x, y, a, b);
			System.out.println("Point (42,99) is on the curve.");
		} catch(Exception e) {
			System.out.println("Point (42,99) is NOT on the curve.");
		}		
		System.out.println();
		
		// Exercise 2 
		System.out.println("***** Exercise 2 *****");
		Element x1 = new Element(from(170), order);
		Element y1 = new Element(from(142), order);
		
		Point p1 = null;
		try {
			p1 = Point.fromCurve(x1, y1, a, b);
		} catch(Exception e) {
			System.out.println("Point (170,142) is NOT on the curve.");
		}
		
		if (p1 != null) {
			Element x2 = new Element(from(60), order);
			Element y2 = new Element(from(139), order);
			
			Point p2 = null;
			try {
				p2 = Point.fromCurve(x2, y2, a, b);
			} catch(Exception e) {
				System.out.println("Point (60,139) is NOT on the curve.");
			}
			
			if (p2 != null) {
				Point p3 = p1.add(p2);
				System.out.println("(170,142) + (60,139) = "+p3);
			}
		}
		
		x1 = new Element(from(47), order);
		y1 = new Element(from(71), order);
		
		p1 = null;
		try {
			p1 = Point.fromCurve(x1, y1, a, b);
		} catch(Exception e) {
			System.out.println("Point (47,71) is NOT on the curve.");
		}
		
		if (p1 != null) {
			Element x2 = new Element(from(17), order);
			Element y2 = new Element(from(56), order);
			
			Point p2 = null;
			try {
				p2 = Point.fromCurve(x2, y2, a, b);
			} catch(Exception e) {
				System.out.println("Point (17,56) is NOT on the curve.");
			}
			
			if (p2 != null) {
				Point p3 = p1.add(p2);
				System.out.println("(47,71) + (17,56) = "+p3);
			}
		}
		
		x1 = new Element(from(143), order);
		y1 = new Element(from(98), order);
		
		p1 = null;
		try {
			p1 = Point.fromCurve(x1, y1, a, b);
		} catch(Exception e) {
			System.out.println("Point (143,98) is NOT on the curve.");
		}
		
		if (p1 != null) {
			Element x2 = new Element(from(76), order);
			Element y2 = new Element(from(66), order);
			
			Point p2 = null;
			try {
				p2 = Point.fromCurve(x2, y2, a, b);
			} catch(Exception e) {
				System.out.println("Point (76,66) is NOT on the curve.");
			}
			
			if (p2 != null) {
				Point p3 = p1.add(p2);
				System.out.println("(143,98) + (76,66) = "+p3);
			}
		}
		System.out.println();
		
		// Exercise 4 
		System.out.println("***** Exercise 4 *****");
		x1 = new Element(from(192), order);
		y1 = new Element(from(105), order);
		
		p1 = null;
		try {
			p1 = Point.fromCurve(x1, y1, a, b);
			p1 = p1.addSelf(BigInteger.TWO);
			System.out.println("2.(192,105) = "+p1);
		} catch(Exception e) {
			System.out.println("Point (192,105) is NOT on the curve.");
		}
		
		x1 = new Element(from(143), order);
		y1 = new Element(from(98), order);
		
		p1 = null;
		try {
			p1 = Point.fromCurve(x1, y1, a, b);
			p1 = p1.addSelf(BigInteger.TWO);
			System.out.println("2.(143,98) = "+p1);
		} catch(Exception e) {
			System.out.println("Point (143,98) is NOT on the curve.");
		}
		
		x1 = new Element(from(47), order);
		y1 = new Element(from(71), order);
		
		p1 = null;
		try {
			p1 = Point.fromCurve(x1, y1, a, b);
			Point p2 = p1.addSelf(BigInteger.TWO);
			System.out.println("2.(47.71) = "+p2);
			p2 = p1.addSelf(from(4));
			System.out.println("4.(47.71) = "+p2);
			p2 = p1.addSelf(from(8));
			System.out.println("8.(47.71) = "+p2);			
			p2 = p1.addSelf(from(21));
			System.out.println("21.(47.71) = "+p2);
		} catch(Exception e) {
			System.out.println("Point (47.71) is NOT on the curve.");
		}		
		System.out.println();
		
		// Exercise 5 
		System.out.println("***** Exercise 5 *****");

		x1 = new Element(from(15), order);
		y1 = new Element(from(86), order);
		
		try {
			p1 = Point.fromCurve(x1, y1, a, b);			
			System.out.println("Group order = "+p1.findGroupOrder());
		} catch(Exception e) {
			System.out.println("Point (15,86) is NOT on the curve.");
		}
		System.out.println();		
		
		// Exercise 6 
		System.out.println("***** Exercise 5 *****");
		Secp256k1Point publicKey = Secp256k1Point.fromCurve(
				new Secp256k1Element(new BigInteger(
						"887387e452b8eacc4acfde10d9aaf7f6d9a0f975aabb10d006e4da568744d06c", 16)), 
				new Secp256k1Element(new BigInteger(
						"61de6d95231cd89026e286df3b6ae4a894a3378e393e93a0f45b666329a0ae34", 16)));
		
		BigInteger z = 
				new BigInteger("ec208baa0fc1c19f708a9ca96fdeff3ac3f230bb4a7ba4aede4942ad003c0f60", 16);
		BigInteger r = 
				new BigInteger("ac8d1c87e51d0d441be8b3dd5b05c8795b48875dffe00b7ffcfac23010d3a395", 16);
		BigInteger s = 
				new BigInteger("68342ceff8935ededd102dd876ffd6ba72d6a427a3edb13d26eb0781cb423c4", 16);
		
		boolean verified = publicKey.verifySignature(z, new Signature(r, s));
		System.out.println("s=0x68342ceff8935ededd102dd876ffd6ba72d6a427a3edb13d26eb0781cb423c4 is "
								+(verified ? "valid." : "invalid"));
		
		z = new BigInteger("7c076ff316692a3d7eb3c3bb0f8b1488cf72e1afcd929e29307032997a838a3d", 16);
		r = new BigInteger("eff69ef2b1bd93a66ed5219add4fb51e11a840f404876325a1e8ffe0529a2c", 16);
		s = new BigInteger("c7207fee197d27c618aea621406f6bf5ef6fca38681d82b2f06fddbdce6feab6", 16);
		
		verified = publicKey.verifySignature(z, new Signature(r, s));
		System.out.println("s=0xc7207fee197d27c618aea621406f6bf5ef6fca38681d82b2f06fddbdce6feab6 is "
								+(verified ? "valid." : "invalid"));
		System.out.println();
		
		// Exercise 7 
		System.out.println("***** Exercise 7 *****");	
		BigInteger e = from(12345);
		BigInteger k = from(1234567890);
		
		String message = "Programming Bitcoin!";		
		z = Crypto.hash256BigInt(message.getBytes()); 
		
		Secret secret = new Secret(e);
		Signature sig = secret.sign(z, k);
		System.out.println(z.toString(16));
		System.out.println(sig.getR().toString(16));
		System.out.println(sig.getS().toString(16));
		
		publicKey = G.multiply(e); // scalar multiplication ...
		verified = publicKey.verifySignature(z, sig);
		System.out.println("signature is "+(verified ? "valid." : "invalid"));
		
		System.out.println();
	}
}
