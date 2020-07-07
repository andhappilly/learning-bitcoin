package bitcoin.util;

import static bitcoin.util.Bytes.newBytes;
import static bitcoin.util.Bytes.reverse;
import static bitcoin.util.Bytes.stitch;
import static bitcoin.util.Bytes.zeroSuffix;
import static bitcoin.util.Bytes.EMPTY_BYTES;

import static bitcoin.util.Functions.checkNull;

import java.math.BigInteger;

public final class BigInt {
	
	private static final byte HEX_FD = (byte)0xfd;
	private static final byte HEX_FE = (byte)0xfe;
	private static final byte HEX_FF = (byte)0xff;
	
	private static final BigInteger HEX_FD_INT = new BigInteger("fd", 16);
	private static final BigInteger HEX_16_POW_4 = new BigInteger("10000", 16);
	private static final BigInteger HEX_16_POW_8 = new BigInteger("100000000", 16);
	private static final BigInteger HEX_16_POW_16 = new BigInteger("10000000000000000", 16);
	
	private BigInt() {}
	
	public static BigInteger from(int v) {
		return BigInteger.valueOf(v);
	}
	
	public static BigInteger from(long v) {
		return BigInteger.valueOf(v);
	}
	
	public static BigInteger gcd(BigInteger a, BigInteger b) {
		checkNull(a, b);
		
		BigInteger g = a.gcd(b);
		return isThisGreaterThanThat(g, BigInteger.ONE) ? g : null;
	}
	
	public static boolean isOdd(BigInteger a) {
		return a.testBit(0);
	}
	
	public static boolean isNegative(BigInteger a) {
		return a.signum() == -1;
	}
	
	public static boolean isGreaterThanZero(BigInteger a) {
		return a.compareTo(BigInteger.ZERO) == 1;
	}
	
	public static boolean isThisLessThanThat(BigInteger thiz, BigInteger that) {
		return thiz.compareTo(that) == -1;
	}
	
	public static boolean isThisGreaterThanThat(BigInteger thiz, BigInteger that) {
		return thiz.compareTo(that) == 1;
	}
	
	public static boolean isThisEqualOrGreaterThanThat(BigInteger thiz, BigInteger that) {
		return thiz.compareTo(that) >= 0;
	}
	
	public static BigInteger fromLittleEndian(byte[] littleEndian) {
		return fromLittleEndian(littleEndian, 0, littleEndian.length);
	}
	
	public static BigInteger fromLittleEndian(byte[] littleEndian, int start, int end) {
		return fromLittleEndian(true, littleEndian, start, end);
	}

	public static BigInteger fromLittleEndian(boolean positiveExpected, byte[] littleEndian, int start, int end) {
		checkNull(littleEndian);
		
		if (littleEndian.length < 1) {
			throw new IllegalArgumentException("Bytes cannot be empty");
		}
		
		reverse(littleEndian);
		
		return positiveExpected ? new BigInteger(1, littleEndian) : new BigInteger(littleEndian);
	}
	
	public static byte[] toLittleEndian(BigInteger a) {
		return toLittleEndian(a, -1);
	}
	
	public static byte[] toLittleEndian(BigInteger a, int bytesToTake) {
		checkNull(a);
		
		if (bytesToTake < 0) {
			throw new IllegalArgumentException("Bytes to take cannot be neagtive.");
		}
		
		if (bytesToTake == 0) {
			return EMPTY_BYTES;
		}
		
		// The default representation is in big-endian format ...
		byte[] aBytes = a.toByteArray();
		
		// Reverse the order of the bytes to convert to little-endian format ...
		reverse(aBytes);
		
		// If a proper sub array is requested that is returned ...
		if (bytesToTake <= aBytes.length) {
			return newBytes(aBytes, 0, bytesToTake);
		}
		
		// Pad with 0 bytes when bytes to take is greater than array length ...
		return zeroSuffix(bytesToTake - aBytes.length, aBytes);
	}
	
	public static int getVarIntSize(byte leadingByte) {
		int bytesToRead = 1;
		// Read the first byte ...
		switch(leadingByte) {
			// This indicates that the next two bytes should be read ... 
			case HEX_FD: bytesToRead = 2; break;
			// This indicates that the next four bytes should be read ... 
			case HEX_FE: bytesToRead = 4; break;
			// This indicates that the next eight bytes should be read ... 
			case HEX_FF: bytesToRead = 8; break;
			// This indicates that the first byte represents the integer ... 
			default: break;
		}
		
		return bytesToRead;
	}
	
	public static BigInteger fromVarInt(byte[] varInt) {
		checkNull(varInt);
		
		if (varInt.length == 0) {
			throw new IllegalArgumentException("Varint array is incorrect.");
		}
		
		if (varInt.length == 1) {
			// First byte represents the integer, hence return the smme ...
			return BigInteger.valueOf(varInt[0]);
		}
		
		// We construct integer from the little endian representation ...
		return fromLittleEndian(varInt);
	}
	
	public static byte[] toVarInt(BigInteger a) {
		checkNull(a);
		
		if (isThisLessThanThat(a, HEX_FD_INT)) {
			return new byte[] {a.byteValue()};
		}
		
		byte prefix;
		byte[] littleEndian = toLittleEndian(a);
		int bytesToTake = 0;
		
		if (isThisLessThanThat(a, HEX_16_POW_4)) {
			prefix = HEX_FD;
			bytesToTake = 2;
		} else if (isThisLessThanThat(a, HEX_16_POW_8)) {
			prefix = HEX_FE;
			bytesToTake = 4;
		} else if (isThisLessThanThat(a, HEX_16_POW_16)) {
			prefix = HEX_FF;
			bytesToTake = 8;
		} else {
			throw new IllegalArgumentException("Integer is too large for the varint type.");
		}
		
		return stitch(prefix, littleEndian, 0, bytesToTake);
	}
}
