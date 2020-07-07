package bitcoin.math.ellipticcurve;

import static bitcoin.util.BigInt.from;
import static bitcoin.util.Functions.areEqual;
import static bitcoin.util.Functions.checkNull;

import java.math.BigInteger;

import bitcoin.math.field.Member;

public class Point<T extends Member<T, V>, V> {
	private static final BigInteger ZERO = BigInteger.ZERO;
	private static final BigInteger ONE = BigInteger.ONE;
	private static final BigInteger TWO = BigInteger.TWO;
	private static final BigInteger THREE = from(3);
			
	private T x, y, a, b;
	private boolean identity;
	
	private transient volatile Point<T, V> infinity;
	private transient volatile String p;
	
	// This constructor is only called to define the point at "Infinity" which is the identity point ...
	protected Point(T a, T b) {
		this.a = a;
		this.b = b;
		
		this.identity = true;
	}
	
	protected Point(T x, T y, T a, T b) {
		this.a = a;
		this.b = b;
		this.x = x;
		this.y = y;
		
		T lhs = y.power(TWO);
		T rhs = xCubePlusAxPlusB(x);
		
		if (!lhs.equals(rhs)) {
			throw new IllegalArgumentException("Given paramters do not satisy an elliptic curve function");
		}
	}	
	
	public boolean equals(Object another) {
		if (another instanceof Point) {
			Point<T, V> other = Point.class.cast(another);
			
			if (this.isIdentity() && !other.isIdentity() ||
					!this.isIdentity() && other.isIdentity()) {
				return false;
			}
			
			if (abEquals(other)) {
				if (this.isIdentity()) {
					return true;
				}
				
				return xyEquals(other);
			}
		}
		
		return false;		
	}
	
	public Point<T, V> add(Point<T, V> other) {
		checkNull(other);
		
		if (!abEquals(other)) {
			throw new IllegalArgumentException("Points are not on the same curve.");
		}
		
		// If this point is added to identity, this point is returned ...
		if (other.isIdentity()) {
			return this;
		}
		
		// If this is identity, return other ...
		if (this.isIdentity()) {
			return other;
		}
		
		// Adding this point to it's inverse should return the identity ...
		if (isInverse(other)) {
			ensureIdentityPoint();
			return infinity;
		}		
		
		T x3;
		T y3;
		if (this.x.equals(other.x)) { // If both points are same, that is the line is a tangent ...
			
			if (this.y.isZero()) { // Means the tangent line is vertical ...
				return infinity;
			}
			
			T two = this.x.getFactory().valueOf(TWO);
			T three = this.x.getFactory().valueOf(THREE);
			T slope = three.multiply(this.x.power(TWO)).add(a).divide(two.multiply(this.y));
			
			x3 = slope.power(TWO).subtract(two.multiply(this.x));
			y3 = slope.multiply(this.x.subtract(x3)).subtract(this.y);
		} else {
			T slope = other.y.subtract(this.y).divide(other.x.subtract(this.x));
			
			x3 = slope.power(TWO).subtract(this.x).subtract(other.x);
			y3 = slope.multiply(this.x.subtract(x3)).subtract(this.y);
		}
		
		return fromCurve(x3, y3, a, b);
	}
	
	public Point<T, V> addSelf(BigInteger nTimes) {
		ensureIdentityPoint();
		Point<T, V> result = infinity;
		Point<T, V> current = this;
		do {			
			if (areEqual(ONE, nTimes.and(ONE))) {
				result = result.add(current);				
			} 
			
			current = current.add(current);
			nTimes = nTimes.shiftRight(1);
		} while(!areEqual(ZERO, nTimes));		
		
		return result;
	}
	
	public T getX() {
		return x;
	}
	
	public T getY() {
		return y;
	}
	
	public V xValue() {
		return x.value();
	}
	
	public V yValue() {
		return y.value();
	}
	
	public BigInteger findGroupOrder() {
		BigInteger gOrder = ZERO;		
		Point<T, V> p2 = this;
		do {
			gOrder = gOrder.add(ONE);
			if (p2.isIdentity()) {					
				break;
			}
			p2 = p2.add(this);
		} while(true);
		
		return gOrder;
	}
	
	public boolean isIdentity() {
		return identity;
	}
	
	public String toString() {
		if (p == null) {
			p = identity ? "(infinity)" : "("+x+","+y+")";
		}
		
		return p;
	}
	
	public static <T extends Member<T, V>, V> Point<T, V> fromCurve(T x, T y, T a, T b) {
		checkNull(x, y, a, b);
		
		return new Point<T, V>(x, y, a, b);
	}
	
	public static <T extends Member<T, V>, V> Point<T, V> identity(T a, T b) {
		checkNull(a, b);
		
		return new Point<T, V>(a, b);
	}
	
	protected T xCubePlusAxPlusB(T x) {
		return x.power(THREE).add(a.multiply(x)).add(b);
	}
	
	protected Point<T, V> addTimesN(Point<T, V> point, Point<T, V> infinity, BigInteger nTimes) {
		Point<T, V> result = infinity;
		Point<T, V> current = point;
		do {			
			if (areEqual(ONE, nTimes.and(ONE))) {
				result = result.add(current);				
			} 
			
			current = current.add(current);
			nTimes = nTimes.shiftRight(1);
		} while(!areEqual(ZERO, nTimes));		
		
		return result;
	}
	
	private boolean isInverse(Point<T, V> other) {
		checkNull(other);
		
		if (abEquals(other)) {
			// The inverse of an identity is itself ...
			if (this.isIdentity() && other.isIdentity()) {
				return true;
			}
			
			return areEqual(this.x, other.x) 
						&& areEqual(this.y, other.y.negate());
		}
		
		return false;
	}
	
	private void ensureIdentityPoint() {
		if (infinity == null) {
			infinity = identity(a, b);
		}
	}
	
	private boolean xyEquals(Point<T, V> other) {
		return areEqual(this.x, other.x) && areEqual(this.y, other.y);
	}
	
	private boolean abEquals(Point<T, V> other) {
		return areEqual(this.a, other.a) && areEqual(this.b, other.b);
	}
}
