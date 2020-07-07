package bitcoin.math.field;

import java.math.BigInteger;

public interface Member <T extends Member<T, V>, V> {
	interface Factory <T extends Member<T, V>, V> {
		T valueOf(BigInteger v);
	}
	
	T add(T o);
	T subtract(T o);
	T multiply(T o);
	T divide(T o);
	T power(BigInteger n);
	T negate();
	boolean isZero();
	V value();
	
	Member.Factory<T, V> getFactory();
}
