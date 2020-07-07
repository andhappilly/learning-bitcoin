package bitcoin.math.field;

import static bitcoin.util.BigInt.from;
import static bitcoin.util.BigInt.gcd;
import static bitcoin.util.BigInt.isNegative;
import static bitcoin.util.BigInt.isOdd;
import static bitcoin.util.Functions.areEqual;
import static bitcoin.util.Functions.checkNull;
import static bitcoin.util.Functions.isNull;

import java.math.BigInteger;

public final class RealNumber implements Member<RealNumber, RealNumber.Fraction>, Member.Factory<RealNumber, RealNumber.Fraction> {	
	public static final class Fraction {
		private BigInteger numerator, denominator;
		
		private Fraction(BigInteger numerator) {
			checkNull(numerator);
			
			this.numerator = numerator;
		}
		
		private Fraction(BigInteger numerator, BigInteger denominator) {
			checkNull(numerator, denominator);
			
			if (BigInteger.ZERO.equals(denominator)) {
				throw new IllegalArgumentException("Division by 0 is undefined.");
			}
			
			this.numerator = numerator;
			this.denominator = denominator;
			
			BigInteger g = gcd(numerator, denominator);
			if (!isNull(g)) {				
				BigInteger d = denominator.divide(g);
				this.numerator = numerator.divide(g);				
				this.denominator = areEqual(BigInteger.ONE, d) ? null : d;
				
			}
		}
		
		public boolean equals(Object another) {
			if (another instanceof Fraction) {
				Fraction other = (Fraction)another;
				return areEqual(this.numerator, other.numerator)
						&& areEqual(this.denominator, other.denominator);
			}
			
			return false;
		}
		
		public String toString() {
			String n = numerator.toString();
			if (isInteger()) {
				return n;
			}
			
			String d = denominator.toString();
			return numerator+"/"+d;
		}
		
		private boolean isInteger() {
			return isNull(denominator);
		}
	}
			
	private static final RealNumber ZERO = new RealNumber(BigInteger.ZERO);		
	private static final RealNumber TWO = new RealNumber(BigInteger.TWO);
	
	private static final BigInteger BIG_INT_3 = from(3);
	private static final BigInteger MAX_LONG = from(Long.MAX_VALUE);
	private static final RealNumber THREE = new RealNumber(BIG_INT_3);
	
	private Fraction fraction;
	
	// Use of this constructor assumes the denominator be 1 ...
	public RealNumber(BigInteger numerator) {
		this.fraction = new Fraction(numerator);
	}
	
	public RealNumber(BigInteger numerator, BigInteger denominator) {
		this.fraction = new Fraction(numerator, denominator);
	}	
	
	public boolean equals(Object another) {
		if (another instanceof RealNumber) {
			RealNumber other = (RealNumber)another;
			return areEqual(this.fraction, other.fraction);
		}
		
		return false;
	}
	
	public String toString() {
		return fraction.toString();
	}
	
	public RealNumber power(BigInteger p) {
		if (fraction.isInteger()) {
			return new RealNumber(revisedModPow(fraction.numerator, p));
		}
		
		return new RealNumber(
				revisedModPow(fraction.numerator, p), 
				revisedModPow(fraction.denominator, p));
	}
	
	public RealNumber multiply(RealNumber v) {
		BigInteger n = fraction.numerator.multiply(v.fraction.numerator);
		
		boolean t = fraction.isInteger();
		boolean o = v.fraction.isInteger();
		
		if (t && o) {
			return new RealNumber(n);
		}
		
		if (t && !o) {				
			return buildValue(n, v.fraction.denominator);
		}
		
		if (!t && o) {
			return buildValue(n, fraction.denominator);
		}
		
		BigInteger d = fraction.denominator.multiply(v.fraction.denominator);			
		return buildValue(n, d);
	}
	
	public RealNumber divide(RealNumber v) {
		boolean t = fraction.isInteger();
		boolean o = v.fraction.isInteger();
		
		if (t && o) {
			return buildValue(fraction.numerator, v.fraction.numerator);
		}
		
		BigInteger n;
		BigInteger d;
		if (t && !o) {
			n = fraction.numerator.multiply(v.fraction.denominator);
			d = v.fraction.numerator;
		} else if (!t && o) {
			n = fraction.numerator;
			d = fraction.denominator.multiply(v.fraction.numerator);
		} else {
			n = fraction.numerator.multiply(v.fraction.denominator);
			d = fraction.denominator.multiply(v.fraction.numerator);
		}
		
		return buildValue(n, d);
	}
	
	public RealNumber add(RealNumber v) {
		return addOrSubtract(v, true);
	}
	
	public RealNumber subtract(RealNumber v) {
		return addOrSubtract(v, false);
	}
	
	public RealNumber negate() {
		if (fraction.isInteger()) {
			return new RealNumber(fraction.numerator.negate());
		}
		
		return new RealNumber(fraction.numerator.negate(), fraction.denominator);
	}
	
	public boolean isZero() {
		return areEqual(BigInteger.ZERO, fraction.numerator);
	}
	
	public Fraction value() {
		return fraction;
	}
	
	public RealNumber valueOf(BigInteger a) {
		if (areEqual(BigInteger.TWO, a)) {
			return TWO;
		}
		
		if (areEqual(BIG_INT_3, a)) {
			return THREE;
		}
		
		return new RealNumber(a);
	}
	
	public Member.Factory<RealNumber, Fraction> getFactory() {
		return this;
	}		
	
	private RealNumber addOrSubtract(RealNumber v, boolean add) {
		boolean t = fraction.isInteger();
		boolean o = v.fraction.isInteger();
		
		if (t && o) {
			if (add) {
				return new RealNumber(fraction.numerator.add(v.fraction.numerator));
			}
			
			return new RealNumber(fraction.numerator.subtract(v.fraction.numerator));
		}
		
		BigInteger d1 = fraction.denominator == null ? BigInteger.ONE : fraction.denominator;
		BigInteger d2 = v.fraction.denominator == null ? BigInteger.ONE : v.fraction.denominator;
		
		BigInteger n = null;
		if (add) {
			n= fraction.numerator.multiply(d2).add(v.fraction.numerator.multiply(d1));
		} else {
			n= fraction.numerator.multiply(d2).subtract(v.fraction.numerator.multiply(d1));
		}
		
		if (areEqual(BigInteger.ZERO, n.abs())) {
			return RealNumber.ZERO;
		}
		
		BigInteger d = d1.multiply(d2);
		
		return buildValue(n, d);
	}
	
	private RealNumber buildValue(BigInteger n, BigInteger d) {
		BigInteger g = gcd(n, d);
		if (isNull(g)) {
			return new RealNumber(n, d);
		}
		
		n = n.divide(g);
		d = d.divide(g);
		
		boolean nN = isNegative(n);
		boolean dN = isNegative(d);
		
		boolean negative = nN && !dN || !nN && dN;
		if (negative) {
			n = !nN ? n.negate() : n;
		} else {
			n = n.abs();				
		}
		
		d = d.abs();
		
		return areEqual(BigInteger.ONE, d) ? new RealNumber(n) : new RealNumber(n, d);			
	}
	
	private BigInteger revisedModPow(BigInteger base, BigInteger e) {
		BigInteger revised = base.abs().modPow(e, MAX_LONG);
		return isNegative(base) && isOdd(e) ? revised.negate() : revised;
	}
}
