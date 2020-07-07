package bitcoin.crypto.ecc;

import static bitcoin.util.Bytes.FORTY_EIGHT;
import static bitcoin.util.Bytes.TWO;
import static bitcoin.util.Bytes.ZERO;
import static bitcoin.util.Bytes.accumulateByteToHex;
import static bitcoin.util.Bytes.hexCharsToByte;
import static bitcoin.util.Bytes.ltrimIndex;
import static bitcoin.util.Functions.areEqual;
import static bitcoin.util.Functions.checkNull;

import java.math.BigInteger;

public final class Signature {
	public static final BigInteger SIGHASH_ALL = BigInteger.ONE;
	public static final String SIGHASH_ALL_MARKER = "01";
	
	private BigInteger r, s;
	
	public Signature(BigInteger r, BigInteger s) {
		checkNull(r, s);
		
		this.r = r;
		this.s = s;
	}
	
	public boolean equals(Object another) {
		if (another instanceof Signature) {
			Signature other = (Signature)another;
			return areEqual(this.r, other.r) &&
						areEqual(this.s, other.s);
		}
		
		return false;
	}
	
	public BigInteger getR() {
		return r;
	}
	
	public BigInteger getS() {
		return s;
	} 
	
	public String toDER() {		
		byte[] rBytes = r.toByteArray();
		byte[] sBytes = s.toByteArray();
		
		// Strip all the leading zero bytes ...
		int rBytesStart = ltrimIndex(rBytes);
		int rLength = rBytes.length - rBytesStart;
		int sBytesStart = ltrimIndex(sBytes);
		int sLength = sBytes.length - sBytesStart;
		
		// Although in ECDSA all numbers are positive, DER is also used to encode negative numbers.
		// Therefore if the highest order byte is negative after stripping all leading zeros, we need 
		// to add an extra zero byte at the beginning ...
		boolean zeroByteForR = rBytes[rBytesStart] < 0;
		boolean zeroByteForS = sBytes[sBytesStart] < 0;
		
		// adjust lengths if zero byte prefix added ...
		rLength = zeroByteForR ? (rLength + 1) : rLength; 
		sLength = zeroByteForS ? (sLength + 1) : sLength;
		
		// total length consists of 2 marker bytes and one byte each to specify the lengths of the 
		// byte arrays (thus 4 bytes) in addition to the actual array sizes .... 
		byte tLength = (byte)(4 + rLength + sLength); 
		
		StringBuilder accumulator = new StringBuilder((2 + tLength)*2);		
		accumulateByteToHex(accumulator, FORTY_EIGHT);
		accumulateByteToHex(accumulator, tLength);
		accumulateByteToHex(accumulator, TWO);
		accumulateByteToHex(accumulator, (byte)rLength);
		if (zeroByteForR) {
			accumulateByteToHex(accumulator, ZERO);
		}
		accumulateByteToHex(accumulator, rBytesStart, rBytes);
		accumulateByteToHex(accumulator, TWO);
		accumulateByteToHex(accumulator, (byte)sLength);
		if (zeroByteForS) {
			accumulateByteToHex(accumulator, ZERO);
		}
		accumulateByteToHex(accumulator, sBytesStart, sBytes);
		
		return accumulator.toString();
	}
	
	public static Signature fromDER(String der) {
		checkNull(der);
		
		
		der = der.trim();
		
		if (!der.startsWith("30")) { // Marker decimal '48' is '30' in hex ...
			// Make sure the DER format has the correct beginning marker ...
			throw new IllegalArgumentException("String not in valid DER format");
		}
		
		// Adjust the length to account for the SIGHASH_ALL suffix marker ...
		int derLength = der.endsWith("01") ?  der.length() - 2 : der.length();
		
		// Next read the next byte that gives the total length  ...
		char c1;
		char c2;
		
		try {
			c1 = der.charAt(2);
			c2 = der.charAt(3);
		} catch(Exception e) {
			throw new IllegalArgumentException("String not in valid DER format", e);
		}
			
		// Multiply by 2 because each byte is 2 characters ...
		int expectedLength = hexCharsToByte(c1, c2) * 2;  		
		
		// We subtract 4 characters to account for the initial marker and total length bytes ...
		//int actualLength = der.length() - 4;
		int actualLength = derLength - 4;
		
		if (expectedLength != actualLength) {
			throw new IllegalArgumentException("String not in valid DER format");
		}
		
		// Next make sure the "02" marker is present ...
		if (!der.regionMatches(4, "02", 0, 2)) {
			throw new IllegalArgumentException("String not in valid DER format");
		}
		
		// Next parse the number of "r" bytes to read ...
		try {
			c1 = der.charAt(6);
			c2 = der.charAt(7);
		} catch(Exception e) {
			throw new IllegalArgumentException("String not in valid DER format", e);
		}
		
		// Once again multiply by 2 because each byte is 2 characters ...
		int rLength = hexCharsToByte(c1, c2) * 2;
		
		// Figure out the starting and ending positions for rBytes ...
		int rStart = 8;
		int rEnd = rStart + rLength;
		
		// Construct 'r' ...
		BigInteger r = new BigInteger(der.substring(rStart, rEnd), 16);
		
		// Next make sure the "02" marker is present ...
		if (!der.regionMatches(rEnd, "02", 0, 2)) {
			throw new IllegalArgumentException("String not in valid DER format");
		}
		
		// Parse the next byte to read the number of "s" bytes to read ...
		// The "length" byte starts after the marker byte, hence skip the next 2 characters ...
		int sLen = rEnd + 2; 
		try {
			c1 = der.charAt(sLen);
			c2 = der.charAt(sLen + 1);
		} catch(Exception e) {
			throw new IllegalArgumentException("String not in valid DER format", e);
		}
		
		// Once again multiply by 2 because each byte is 2 characters ...
		int sLength = hexCharsToByte(c1, c2) * 2;
		
		// Figure out the starting and ending positions for sBytes ...
		int sStart = sLen + 2;
		int sEnd = sStart + sLength;
		
		// Construct 's' ...
		BigInteger s = new BigInteger(der.substring(sStart, sEnd), 16);
		
		// Finally return the signature ...
		return new Signature(r, s);
	}
	
	public static void main(String[] args) {
		String der = "3045022037206a0610995c58074999cb9767b87af4c4978db68c06e8e6e81d282047a7c60221008ca63759c1157ebeaec0d03cecca119fc9a75bf8e6d0fa65c841c8e2738cdaec";
		//String der = "304402207e6009ad86367fc4b166bc80bf10cf1e78832a01e9bb491c6d126ee8aa436cb502200e29e6dd7708ed419cd5ba798981c960f0cc811b24e894bff072fea8074a7c4c01";
		//String der = "304402207e6009ad86367fc4b166bc80bf10cf1e78832a01e9bb491c6d126ee8aa436cb502200e29e6dd7708ed419cd5ba798981c960f0cc811b24e894bff072fea8074a7c4c";
		Signature sig = Signature.fromDER(der);
		
		System.out.println("r = "+sig.r.toString(16));
		System.out.println("s = "+sig.s.toString(16));
	}
}
