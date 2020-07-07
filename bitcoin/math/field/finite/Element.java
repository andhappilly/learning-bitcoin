package bitcoin.math.field.finite;

import static bitcoin.util.BigInt.isThisEqualOrGreaterThanThat;
import static bitcoin.util.Functions.areEqual;

import java.math.BigInteger;

import bitcoin.math.field.Member;

public class Element implements Member<Element, BigInteger>, Member.Factory<Element, BigInteger> {
	private BigInteger number;
	private Operator operator;
	
	private final static class Operator {
		private BigInteger order;
		private transient volatile BigInteger orderMinusOne;
		
		private Operator(BigInteger order) {
			this.order = order;
		}
		
		private BigInteger getOrder() {
			return order;
		}
		
		private BigInteger add(BigInteger a, BigInteger b) {
			checkBounds(a, order);	
			checkBounds(b, order);
			
			return element(a.add(b));
		}
		
		private BigInteger subtract(BigInteger a, BigInteger b) {
			checkBounds(a, order);	
			checkBounds(b, order);
			
			return element(a.subtract(b));
		}
		
		private BigInteger multiply(BigInteger a, BigInteger b) {
			checkBounds(a, order);	
			checkBounds(b, order);
			
			return element(a.multiply(b));
		}
		
		private BigInteger divide(BigInteger a, BigInteger b) {
			checkBounds(a, order);	
			checkBounds(b, order);
			
			// Note that division by '0' is undefined ... 
			if (areEqual(BigInteger.ZERO, b)) {
				throw new IllegalArgumentException("Division by 0 is undefined.");
			}
			
			// Under finite field algebra, division of 'a' by 'b' can be defined 
			// as (a * b^-1) ...
			return element(a.multiply(multiplicativeInverse(b)));
		}
		
		private BigInteger power(BigInteger a, BigInteger b) {
			checkBounds(a, order);	
			ensureOrderMinusOne(order);
								
			// Remember any number raised to (order - 1) where order is prime is '1'
			// under finite field algebra. We make use of this fact to reduce the size
			// of the exponent, by finding the remainder when divided by (order - 1).
			// So instead of the original exponent, we can simply use the remainder ...
			//int r = Math.abs(b) % orderMinusOne.intValue();
			BigInteger e = b.remainder(orderMinusOne);
			
			//BigInteger e = bigInt(b < 0 ? -r : r);			
			return element(a.modPow(e, order));
		}
		
		private BigInteger negate(BigInteger a) {
			checkBounds(a, order);	
			if (areEqual(BigInteger.ZERO, a)) {
				return a;
			}
			
			return a.negate();
		}
		
		private boolean isZero(BigInteger a) {
			return areEqual(BigInteger.ZERO, a);
		}
		
		private BigInteger multiplicativeInverse(BigInteger a) {
			checkBounds(a, order);
			
			// Under finite field algebra, multiplicative inverse of an element 'a'
			// is defined as (a ^ (order -2) % order) ...
			return element(a.modPow(order.subtract(BigInteger.TWO), order));
		}
		
		private BigInteger element(BigInteger n) {
			return isNegative(n) ? order.add(n).mod(order) : n.mod(order);
		}
		
		public boolean equals(Object other) {
			if (other instanceof Operator) {
				Operator given = (Operator)other;			
				return this.order.equals(given.order);
			}
			
			return false;
		}
		
		private void ensureOrderMinusOne(BigInteger order) {
			if (orderMinusOne == null) {
				orderMinusOne = order.subtract(BigInteger.ONE);
			}
		}
		
		private void  checkBounds(BigInteger n, BigInteger order) {
			if (isThisEqualOrGreaterThanThat(n, order) || isNegative(n)) {	
				ensureOrderMinusOne(order);
				throw new IllegalArgumentException("Number '"+n+"' not in field range 0 to '"+orderMinusOne);
			}
		}
		
		private static boolean isNegative(BigInteger number) {
			return number.signum() == -1;
		}
	}
	
	public Element(BigInteger number, BigInteger order) {
		this.number = number;
		this.operator = new Operator(order);
		
		operator.checkBounds(number, order);
	}
	
	public BigInteger value() {
		return number;
	}
	
	public BigInteger order() {
		return operator.getOrder();
	}
	
	public String toString() {
		return number.toString();
	}
	
	public boolean equals(Object other) {
		if (other instanceof Element) {
			Element given = (Element)other;			
			return areEqual(this.number, given.number) && 
					areEqual(this.order(), given.order());
		}
		
		return false;
	}
	
	public Element add(Element other) {
		checkCompatible(other);
		
		return elementFor(operator.add(this.number, other.number));
	}
	
	public Element subtract(Element other) {
		checkCompatible(other);	
		
		return elementFor(operator.subtract(this.number, other.number));
	}
	
	public Element multiply(Element other) {
		checkCompatible(other);
		
		return elementFor(operator.multiply(this.number, other.number));
	}
	
	public Element divide(Element other) {
		checkCompatible(other);
		
		return elementFor(operator.divide(this.number, other.number));
	}
	
	public Element power(BigInteger other) {
		return elementFor(operator.power(this.number, other));
	}
	
	public Element negate() {
		return elementFor(operator.negate(this.number));
	}
	
	public boolean isZero() {
		return operator.isZero(this.number);
	}
	
	public Element valueOf(BigInteger a) {
		return elementFor(a);
	}
	
	public Member.Factory<Element, BigInteger> getFactory() {
		return this;
	}
	
	private Element elementFor(BigInteger n) {
		return new Element(operator.element(n), order());
	}
	
	private void checkCompatible(Element other) {
		if (other == null) {
			throw new NullPointerException("'other' cannot be null");
		}
		
		if (this.order() != other.order()) {
			throw new IllegalArgumentException("'other' is not of the same order as this field element.");
		}
	}
}
