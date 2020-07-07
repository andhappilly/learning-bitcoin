package ch2;

import static bitcoin.util.BigInt.from;

import bitcoin.math.ellipticcurve.Point;
import bitcoin.math.field.RealNumber;

public final class Exercises {
	private Exercises() {}
	
	public static void main(String[] args) {
		// Exercise 1 
		System.out.println("***** Exercise 1 *****");
		RealNumber a = new RealNumber(from(5));
		RealNumber b = new RealNumber(from(7));
		
		RealNumber x = new RealNumber(from(2));
		RealNumber y = new RealNumber(from(4));
		try {
			Point.fromCurve(x, y, a, b);
			System.out.println("Point (2,4) is on the curve.");
		} catch(Exception e) {
			System.out.println("Point (2,4) is NOT on the curve.");
		}
		
		x = new RealNumber(from(-1));
		y = new RealNumber(from(-1));
		
		try {
			Point.fromCurve(x, y, a, b);
			System.out.println("Point (-1,-1) is on the curve.");
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("Point (-1,-1) is NOT on the curve.");
		}
		
		x = new RealNumber(from(18));
		y = new RealNumber(from(77));
		
		try {
			Point.fromCurve(x, y, a, b);
			System.out.println("Point (18,77) is on the curve.");
		} catch(Exception e) {
			System.out.println("Point (18,77) is NOT on the curve.");
		}
		
		x = new RealNumber(from(18));
		y = new RealNumber(from(-77));
		
		try {
			Point.fromCurve(x, y, a, b);
			System.out.println("Point (18,-77) is on the curve.");
		} catch(Exception e) {
			System.out.println("Point (18,-77) is NOT on the curve.");
		}
		
		x = new RealNumber(from(5));
		y = new RealNumber(from(7));
		
		try {
			Point.fromCurve(x, y, a, b);
			System.out.println("Point (5,7) is on the curve.");
		} catch(Exception e) {
			System.out.println("Point (5,7) is NOT on the curve.");
		}
		System.out.println();
		
		System.out.println("***** Exercise 4 *****");
		RealNumber x1 = new RealNumber(from(2));
		RealNumber y1 = new RealNumber(from(5));
		
		Point p1 = null;
		try {
			p1 =	Point.fromCurve(x1, y1, a, b);			
		} catch(Exception e) {
			System.out.println("Point (2,5) is NOT on the curve.");
		}
		
		if (p1 != null) {		
			RealNumber x2 = new RealNumber(from(-1));
			RealNumber y2 = new RealNumber(from(-1));
			
			Point p2 = null;
			try {
				p2 =	Point.fromCurve(x2, y2, a, b);			
			} catch(Exception e) {
				System.out.println("Point (-1,-1) is NOT on the curve.");
			}
			
			if (p2 != null) {
				Point p3 = p1.add(p2);
				System.out.println("(2,5) + (-1,-1) = "+p3);
			}
		}		
		System.out.println();
		
		System.out.println("***** Exercise 6 *****");
		x1 = new RealNumber(from(-1));
		y1 = new RealNumber(from(-1));
		
		p1 = null;
		try {
			p1 =	Point.fromCurve(x1, y1, a, b);			
		} catch(Exception e) {
			System.out.println("Point (-1,-1) is NOT on the curve.");
		}
		
		if (p1 != null) {		
			RealNumber x2 = new RealNumber(from(-1));
			RealNumber y2 = new RealNumber(from(-1));
			
			Point p2 = null;
			try {
				p2 =	Point.fromCurve(x2, y2, a, b);			
			} catch(Exception e) {
				System.out.println("Point (-1,-1) is NOT on the curve.");
			}
			
			if (p2 != null) {
				Point p3 = p1.add(p2);
				System.out.println("(-1,-1) + (-1,-1) = "+p3);
			}
		}		
		System.out.println();
	}
}
