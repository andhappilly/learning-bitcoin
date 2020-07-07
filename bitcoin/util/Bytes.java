package bitcoin.util;

import static bitcoin.util.BigInt.from;
import static bitcoin.util.BigInt.isThisGreaterThanThat;
import static bitcoin.util.Functions.checkNull;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

public final class Bytes {
	public static final byte ZERO = 0;
	public static final byte ONE = 1;
	public static final byte TWO = 2;
	public static final byte THREE = 3;
	public static final byte FOUR = 4;
	public static final byte FORTY_EIGHT = 48;
	
	public static final byte[] EMPTY_BYTES = new byte[0];
	
	private static final String BASE_58_SYMBOLS = 
			"123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
	
	private static final BigInteger FIFTY_EIGHT = from(58);
	
	private Bytes() {}
	
	public static byte[] get(int howMany, byte fillWith) {
		byte[] b = new byte[howMany];
		for(int i = 0; i < howMany; ++i) {
			b[i] = fillWith;
		}
		
		return b;
	}
	
	public static boolean isNullOrEmpty(byte[] a) {
		return a == null || a.length == 0;
	}
	
	public static void reverse(byte[] given) {
		checkNull(given);
		
		if (given.length < 2) {
			return;
		}
		
		int steps = given.length / 2;
		for(int i = 0; i < steps; ++i) {
			byte tmp = given[i];
			given[i] = given[given.length - i -1];
			given[given.length - i -1] = tmp;
		}
	}
	
	public static Character[] toCharacters(byte b) {
		Character[] chars = new Character[2];
		// Isolate the higher order 4 bits first, and then find the character representation ... 
		chars[0] = Character.forDigit((b >> 4) & 0xF, 16);
		// Next isolate the lower order 4 bits, and then find the character representation ... 
		chars[1] = Character.forDigit(b & 0xF, 16);
		
		return chars;
	}
	
	public static void accumulateByteToHex(StringBuilder accumulator, byte ... bytes) {
		accumulateByteToHex(accumulator, 0, bytes);
	}
	
	public static void accumulateByteToHex(StringBuilder accumulator, int startAt, byte ... bytes) {
		if (isNullOrEmpty(bytes)) {
			throw new IllegalArgumentException("Bytes cannot be null");
		}
		
		accumulateByteToHex(accumulator, startAt, bytes.length, bytes);
	}
	
	public static void accumulateByteToHex(StringBuilder accumulator, int startAt, int endAt, byte ... bytes) {
		if (isNullOrEmpty(bytes)) {
			throw new IllegalArgumentException("Bytes cannot be null");
		}
		
		if (startAt < 0) {
			throw new IllegalArgumentException("Array index cannot be negative.");
		}
		
		for (int i = startAt; i < endAt;++i) {
			byte b = bytes[i];
			Character[] chars = toCharacters(b);			
			accumulator.append(chars[0]);
			accumulator.append(chars[1]);
		}
	}
	
	public static String bytesToHex(byte[] given) {
		return bytesToHex(given, 0, given.length);
	}
	
	public static String bytesToHex(byte[] given, int start, int end) {		
		if (isNullOrEmpty(given)) {
			throw new IllegalArgumentException("Bytes cannot be null");
		}
		
		StringBuilder accumulator = new StringBuilder(2*given.length);
		
		accumulateByteToHex(accumulator, start, end, given);
		
		return accumulator.toString();
	}
	
	public static byte hexCharsToByte(char hex1, char hex2) {
		byte b1 = (byte)Character.getNumericValue(hex1);
		byte b2 = (byte)Character.getNumericValue(hex2);
		return (byte)(b1 << 4 | b2);
	}
	
	public static byte[] hexToBytes(String hex) {		
		checkNull(hex);
		
		hex = hex.trim();
		return hexToBytes(hex.toCharArray());
	}
	
	public static byte[] hexToBytes(char[] hex) {
		checkNull(hex);
		
		return hexToBytes(hex, 0, hex.length);
	}
	
	public static byte[] hexToBytes(char[] hex, int start, int end) {
		checkNull(hex);
		
		if (start < 0 || start >= end) {
			throw new IllegalArgumentException("Stating index is not correct.");
		}
		
		if (end > hex.length || end < 1) {
			throw new IllegalArgumentException("Ending index is not correct.");
		}
		
		int length = end - start;		
		
		if (length % 2 != 0) {
			throw new IllegalArgumentException("Hex not valid.");
		}
		
		int halfLength = length / 2;
		byte[] result = new byte[halfLength];
		int i = 0;
		int index = start;
		while (index < hex.length) {			
			byte r = hexCharsToByte(hex[index], hex[index + 1]);
			index = index + 2;
			result[i++] = r;
		}
		
		return result;
	}
	
	public static String bytesToBase58(byte[] given) {		
		if (isNullOrEmpty(given)) {
			throw new IllegalArgumentException("Bytes cannot be null");
		}
		
		// Note that we build the result in the reverse order (this saves us from
		// creating a new string every time) and then finally we do a reverse of
		// the entire buffer, this gives us the result we want ...
		BigInteger b = new BigInteger(1, given);
		StringBuilder result = new StringBuilder();
		while (isThisGreaterThanThat(b, BigInteger.ZERO)) {
			BigInteger[] r = b.divideAndRemainder(FIFTY_EIGHT);
			result.append(BASE_58_SYMBOLS.charAt(r[1].intValue()));
			b = r[0];
		}
		
		// Add the leading zero bytes ...
		int count = ltrimIndex(given);
		if (count > 0) {
			for (int i = 0; i < count; i++) {
				result.append('1');
			}
		}
		
		return result.reverse().toString();
	}
	
	public static byte[] base58ToBytes(String b58) {
		checkNull(b58);
		
		BigInteger n = BigInteger.ZERO;
		for (int i = 0; i < b58.length(); ++i) {
			n = n.multiply(FIFTY_EIGHT);
			n = n.add(from(BASE_58_SYMBOLS.indexOf(b58.charAt(i))));
		}
		
		return n.toByteArray();
	}
	
	public static byte[] intToBytes(int v) {
		return ByteBuffer.allocate(4).putInt(v).array();
	}
	
	public static int bytesToInt(byte[] b) {
		checkNull(b);
		
		return ByteBuffer.wrap(b).getInt();
	}
	
	// If the given array length is less than or equal to the minimum
	// length required, then the difference between the 2 is returned.
	// This negative or zero value can be used by the caller to "pad" 
	// if necessary to fit the size. 
	public static int ltrimFitToSizeIndex(byte[] given, int sizeToFit) {
		checkNull(given);
		
		int diff = given.length - sizeToFit;
		
		if (diff <= 0) {
			return diff;
		}		
		
		int index = Integer.MIN_VALUE;
		for (int i = 0; i < diff; ++i) {
			if (given[i] == 0) {
				index = i+1;
			}
		}
		
		if (index == Integer.MIN_VALUE) {
			throw new IllegalArgumentException("Cannot trim to specified length.");
		}
		
		return index;
	}
	
	public static int ltrimIndex(byte[] given) {
		checkNull(given);
		
		for (int i = 0; i < given.length; ++i) {
			if (given[i] != 0) {				
				return i;
			}
		}		
		
		return -1;
	}
	
	public static byte[] zeroPrefix(int prefixCount, byte[] prefixTo) {
		return prefix(ZERO, prefixCount, prefixTo);
	}
	
	public static byte[] prefix(byte prefix, int prefixCount, byte[] prefixTo) {
		checkNull(prefixTo);
		
		if (prefixCount < 0) {
			throw new IllegalArgumentException("Count cannot be negative.");
		}
		
		if (prefixCount == 0) {
			return prefixTo;
		}
		
		byte[] prefixed = new byte[prefixTo.length + prefixCount];
		for (int i = 0; i < prefixCount; ++i) {
			prefixed[i] = prefix;
		}
		
		System.arraycopy(prefixTo, 0, prefixed, prefixCount, prefixTo.length);		
		return prefixed;
	}
	
	public static byte[] zeroSuffix(int suffixCount, byte[] suffixTo) {
		return suffix(ZERO, suffixCount, suffixTo);
	}
	
	public static byte[] suffix(byte suffix, int suffixCount, byte[] suffixTo) {
		checkNull(suffixTo);
		
		if (suffixCount < 0) {
			throw new IllegalArgumentException("Count cannot be negative.");
		}
		
		if (suffixCount == 0) {
			return suffixTo;
		}
		
		byte[] suffixed = new byte[suffixTo.length + suffixCount];
		System.arraycopy(suffixTo, 0, suffixed, 0, suffixTo.length);
		
		for (int i = 0; i < suffixCount; ++i) {
			suffixed[suffixTo.length + i] = suffix;
		}
		
		return suffixed;
	}
	
	public static byte[] stitch(byte prefix, byte[] middle, byte suffix) {		
		checkNull(middle);
		
		return stitch(true, prefix, middle, 0, middle.length, true, suffix);
	}
	
	public static byte[] stitch(byte prefix, byte[] middle) {		
		checkNull(middle);
		
		return stitch(true, prefix, middle, 0, middle.length, false, ZERO);
	}
	
	public static byte[] stitch(byte prefix, byte[] middle, int middleStart) {		
		checkNull(middle);
		
		return stitch(prefix, middle, middleStart, middle.length);
	}
	
	public static byte[] stitch(byte prefix, byte[] middle, int middleStart, int middleEnd) {		
		checkNull(middle);
		
		return stitch(true, prefix, middle, middleStart, middleEnd, false, ZERO);
	}
	
	public static byte[] stitch(byte[] middle, byte suffix) {	
		checkNull(middle);
		
		return stitch(false, ZERO, middle, 0, middle.length, true, suffix);
	}
	
	public static byte[] suffix(byte[] given, byte[] suffixBytes, int suffixBytesStart, int suffixBytesEnd) {
		checkNull(given, suffixBytes);
		
		if (suffixBytesStart < 0 || suffixBytesStart > (suffixBytes.length - 1)) {
			throw new ArrayIndexOutOfBoundsException(suffixBytesStart);
		}
		
		if (suffixBytesEnd <= suffixBytesStart || suffixBytesEnd > suffixBytes.length) {
			throw new ArrayIndexOutOfBoundsException(suffixBytesEnd);
		}
		
		int suffixCopyCount = suffixBytesEnd - suffixBytesStart;
		byte[] suffixed = new byte[given.length + suffixCopyCount];
		System.arraycopy(given, 0, suffixed, 0, given.length);
		System.arraycopy(suffixBytes, suffixBytesStart, suffixed, given.length, suffixCopyCount);
		
		return suffixed;
	}
	
	public static byte[] combine(byte prefix, byte[] a, int aStart, byte[] b, int bStart) {
		return combine(prefix, a, aStart, a.length, b, bStart, b.length);
	}
	
	public static byte[] combine(byte prefix, byte[] a, int aStart, int aEnd, byte[] b, int bStart, int bEnd) {
		return combine(true, prefix, a, aStart, aEnd, b, bStart, bEnd);
	}
	
	public static byte[] combine(byte[] a, int aStart, int aEnd, byte[] b, int bStart, int bEnd) {
		return combine(false, ZERO, a, aStart, aEnd, b, bStart, bEnd);
	}
	
	private static byte[] combine(boolean prefixNeeded, byte prefix, byte[] a, int aStart, int aEnd, byte[] b, int bStart, int bEnd) {
		checkNull(a, b);
		
		if (aStart < 0 || aStart > (a.length - 1)) {
			throw new ArrayIndexOutOfBoundsException(aStart);
		}
		
		if (aEnd <= aStart || aEnd > a.length) {
			throw new ArrayIndexOutOfBoundsException(aEnd);
		}
		
		if (bStart < 0 || bStart > (b.length - 1)) {
			throw new ArrayIndexOutOfBoundsException(bStart);
		}
		
		if (bEnd <= bStart || bEnd > b.length) {
			throw new ArrayIndexOutOfBoundsException(bEnd);
		}
		
		int length = aEnd + bEnd - aStart - bStart;
		if (prefixNeeded) {
			length = length + 1;
		}
		
		byte[] combination = new byte[length];
		
		int aCopyLoc = 0;
		int bCopyLoc = aEnd - aStart;
		if (prefixNeeded) {
			combination[0] = prefix;
			aCopyLoc = aCopyLoc + 1;
			bCopyLoc = bCopyLoc + 1;
		} 
		
		System.arraycopy(a, aStart, combination, aCopyLoc, aEnd - aStart);
		System.arraycopy(b, bStart, combination, bCopyLoc, bEnd - bStart);
		
		return combination;
	}
	
	public static byte[] concatenate(byte[] ... bArrays) {
		int size = 0;
		for(byte[] bA: bArrays) {
			checkNull(bA);
			size += bA.length;
		}
		
		if (size == 0) {
			return EMPTY_BYTES;
		}
		
		byte[] concatenated = new byte[size];
		int start = 0;
		
		for(byte[] bA: bArrays) {
			if (bA.length > 0) {
				System.arraycopy(bA, 0, concatenated, start, bA.length);
				start += bA.length;
			}			
		}
		
		return concatenated;
	}
	
	public static byte[] ensureSize(byte[] source, int size) {
		checkNull(source);
		
		if (source.length == 4) {
			return source;
		}
		
		if (source.length > size) {
			return newBytes(source, source.length - size, source.length);
		}
		
		return suffix((byte)0, size - source.length, source);
	}
	
	public static byte[] newBytes(int length, byte fillWith) {
		if (length < 0) {
			throw new IllegalArgumentException("Array length cannot be less than zero.");
		}
		
		byte[] b = new byte[length];
		Arrays.fill(b, fillWith);
		return b;
	}
	
	public static byte[] newBytes(byte[] source) {
		return newBytes(source, 0, source.length);
	}
	
	public static byte[] newBytes(byte[] source, int from, int to) {
		checkNull(source);
		
		if (from < 0 || from >= source.length) {
			throw new ArrayIndexOutOfBoundsException("Array index not valid.");
		}
		
		if (to <= from || to > source.length) {
			throw new ArrayIndexOutOfBoundsException("Array index not valid.");
		}
		
		int count = to - from;
		byte[] result = new byte[count];
		System.arraycopy(source, from, result, 0, count);
		
		return result;
	}
	
	public static boolean areEqual(byte[] one, int start, int end, byte[] two, int start2, int end2) {
		checkNull(one, two);
		
		if (start < 0 || start >= one.length) {
			throw new ArrayIndexOutOfBoundsException("Array index not valid.");
		}
		
		if (end <= start || end > one.length) {
			throw new ArrayIndexOutOfBoundsException("Array index not valid.");
		}
		
		if (start2 < 0 || start2 >= two.length) {
			throw new ArrayIndexOutOfBoundsException("Array index not valid.");
		}
		
		if (end2 <= start2 || end2 > two.length) {
			throw new ArrayIndexOutOfBoundsException("Array index not valid.");
		}
		
		int length = end - start;
		int length2 = end2 - start2;
		
		if (length != length2) {
			return false;
		}
		
		for (int i = start, j = start2; i < end; ++i, ++j) {
			if (one[i] != two[j]) {
				return false;
			}
		}
		
		return true;
	}
	
	private static byte[] stitch(boolean needPrefix, byte prefix, byte[] middle, 
					int middleStart, int middleEnd, boolean needSuffix, byte suffix) {		
		if (middleStart < 0 || middleStart > (middle.length - 1)) {
			throw new ArrayIndexOutOfBoundsException(middleStart);
		}
		
		if (middleEnd <= middleStart || middleEnd > middle.length) {
			throw new ArrayIndexOutOfBoundsException(middleEnd);
		}
		
		int mLength = middleEnd - middleStart;
		int size = needPrefix && needSuffix ? mLength + 2 : mLength + 1;
		byte[] stitched = new byte[size];
		if (needPrefix) {
			stitched[0] = prefix;					
		} 
		
		System.arraycopy(middle,middleStart, stitched, needPrefix ? 1 : 0, mLength);	
		
		if (needSuffix) {
			stitched[stitched.length - 1] = suffix;
		}
		
		
		return stitched;
	}
}
