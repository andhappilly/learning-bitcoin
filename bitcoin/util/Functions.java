package bitcoin.util;

public final class Functions {
	private Functions() {}
	
	public static boolean isNull(Object a) {
		return a == null;
	}
	
	public static void checkNullOrEmpty(String param) {
		checkNull(param);
		if (param.trim().length() == 0) {
			throw new IllegalArgumentException("Parameter cannot be null or empty");
		}
	}
	
	public static void checkNull(Object ... parameters) {
		if (parameters == null) {
			throw new NullPointerException("Parameters cannot be null");
		}
		
		for (Object p: parameters) {
			if (p == null) {
				throw new NullPointerException("Parameters cannot be null");
			}
		}
	}
	
	public static boolean areEqual(Object a, Object b) {
		boolean aNull = a == null;
		boolean bNull = b == null;
		if (aNull && bNull) {
			return true;
		}
		
		if (aNull && !bNull || !aNull && bNull) {
			return false;
		}
		
		return a.equals(b);
	}
}
