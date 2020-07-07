package bitcoin.crypto.ecc;

import static bitcoin.crypto.ecc.Secp256k1.A;
import static bitcoin.crypto.ecc.Secp256k1.B;
import static bitcoin.crypto.ecc.Secp256k1.G;
import static bitcoin.crypto.ecc.Secp256k1.N;
import static bitcoin.util.BigInt.isNegative;
import static bitcoin.util.BigInt.isOdd;
import static bitcoin.util.Bytes.FOUR;
import static bitcoin.util.Bytes.THREE;
import static bitcoin.util.Bytes.TWO;
import static bitcoin.util.Bytes.accumulateByteToHex;
import static bitcoin.util.Bytes.combine;
import static bitcoin.util.Bytes.ltrimFitToSizeIndex;
import static bitcoin.util.Bytes.stitch;
import static bitcoin.util.Bytes.zeroPrefix;
import static bitcoin.util.Functions.areEqual;
import static bitcoin.util.Functions.checkNull;

import java.math.BigInteger;

import bitcoin.math.ellipticcurve.Point;
import bitcoin.math.field.finite.Element;

public final class Secp256k1Point extends Point<Element, BigInteger> {	
	private static transient volatile Secp256k1Point identity;
	
	private Secp256k1Point() {
		super(A, B);
	}
	
	private Secp256k1Point(Secp256k1Element x, Secp256k1Element y) {
		super(x, y, A, B);
	}
	
	public Secp256k1Point multiply(BigInteger scalar) {
		Point<Element, BigInteger> result = addSelf(scalar);
		return fromCurve(
				new Secp256k1Element(result.xValue()), 
				new Secp256k1Element(result.yValue()));
	}
	
	public boolean equals(Object another) {
		if (another instanceof Secp256k1Point) {
			return super.equals(another);
		}
		
		return false;
	}
	
	public Point<Element, BigInteger> addSelf(BigInteger nTimes) {
		checkNull(nTimes);
		// Since the group of 'G' is positive, negaitve value of 'nTimes' is meaningless ...
		if (isNegative(nTimes)) {
			throw new IllegalArgumentException("Number of times cannot be negative.");
		}
		
		// If 'nTimes' is zero, we return the point at infinity ... 
		if (areEqual(BigInteger.ZERO, nTimes)) {
			return identity();
		}
		
		// Since adding N times the same point results in identity or zero, the
		// actual addition should only be N % nTimes ...
		BigInteger nTimesAdjusted = nTimes.mod(N);
		return super.addSelf(nTimesAdjusted);
	}
	
	public boolean verifySignature(BigInteger z, Signature signature) {
		checkNull(z, signature);
		
		// The formula for the inverse uses Fermat's little theorem, that is
		// s ^ -1 = s ^ -2 % N, since this is a finite group of order N ...
		BigInteger sInverse = signature.getS().modPow(N.subtract(BigInteger.TWO), N);
		
		// The following makes use of the fact that:
		// uG + vP = kG = R
		// Note that we use 'this' point as the public key in the calculation ...
		BigInteger r = signature.getR();
		BigInteger u = z.multiply(sInverse).mod(N);
		BigInteger v = r.multiply(sInverse).mod(N);
		Point<Element, BigInteger> R = G.addSelf(u).add(this.addSelf(v));
		return areEqual(R.xValue(), r);
	}
	
	public byte[] toSecBytes(boolean compressed) {	
		BigInteger x = xValue();
		BigInteger y = yValue();
		
		byte[] xBytes = x.toByteArray();
		// Since all numbers in a finite field are positive, we can safely
		// drop all leading '0' bytes as long as 32 bytes are still there ...
		int xStart = ltrimFitToSizeIndex(xBytes, 32);
		// Check if prefix padding required ...
		if (xStart < 0) {
			// Add zero prefix padding ...
			xBytes = zeroPrefix(-xStart, xBytes);
			xStart = 0;
		}
		
		byte[] result; 
		if (compressed) {
			// Prefix the marker of '2' or '3' to the x value bytes ...
			result = stitch(isOdd(y) ? THREE : TWO, xBytes, xStart);
		} else {
			byte[] yBytes = y.toByteArray();
			// Since all numbers in a finite field are positive, we can safely
			// drop all leading '0' bytes as long as 32 bytes are still there ...
			//int yStart = yBytes.length == 33 ? 1 : 0;
			int yStart = ltrimFitToSizeIndex(yBytes, 32);
			// Check if prefix padding required ...
			if (yStart < 0) {
				// Add zero prefix padding ...
				yBytes = zeroPrefix(-yStart, yBytes);
				yStart = 0;
			}
			
			// Prefix with marker '4' and combine the x value and y value bytes ...
			result = combine(FOUR, xBytes, xStart, yBytes, yStart);
		}
		
		return result; 
	}
	
	public String toSEC() {	
		return toSEC(true);
	}
	
	public String toSEC(boolean compressed) {	
		BigInteger x = xValue();
		BigInteger y = yValue();
		
		StringBuilder accumulator;
		byte prefix;
		if (compressed) {
			accumulator = new StringBuilder(66);
			prefix = isOdd(y) ? THREE : TWO;			
		} else {
			accumulator = new StringBuilder(130);
			prefix = FOUR;
		}
		
		accumulateByteToHex(accumulator, prefix);
		
		byte[] xBytes = x.toByteArray();
		// Since all numbers in a finite field are positive, we can safely
		// drop all leading '0' bytes as long as 32 bytes are still there ...
		int xStart = ltrimFitToSizeIndex(xBytes, 32);
		// Check if prefix padding required ...
		if (xStart < 0) {
			// Add zero prefix padding ...
			xBytes = zeroPrefix(-xStart, xBytes);
			xStart = 0;
		}
		
		accumulateByteToHex(accumulator, xStart, xBytes);
		if (!compressed) {
			byte[] yBytes = y.toByteArray();
			// Since all numbers in a finite field are positive, we can safely
			// drop all leading '0' bytes as long as 32 bytes are still there ...
			int yStart = ltrimFitToSizeIndex(yBytes, 32);
			// Check if prefix padding required ...
			if (yStart < 0) {
				// Add zero prefix padding ...
				yBytes = zeroPrefix(-yStart, yBytes);
				yStart = 0;
			}
			accumulateByteToHex(accumulator, yStart, yBytes);
		}
		
		return accumulator.toString();
	}
	
	public static Secp256k1Point fromSEC(String sec) {
		checkNull(sec);
		
		sec = sec.trim();
		int length = sec.length();
		if (length != 66 && length != 130) {
			throw new IllegalArgumentException("A valid string in SEC fromat is expected.");
		}
		
		String prefix = sec.substring(0, 2);
		boolean prefix2 = "02".equals(prefix);
		boolean prefix3 = "03".equals(prefix);
		boolean prefix4 = "04".equals(prefix);
		
		if (!prefix2 && !prefix3 && !prefix4) {
			throw new IllegalArgumentException("A valid string in SEC fromat is expected.");
		}
		
		if (prefix4 && length != 130) {
			throw new IllegalArgumentException("A valid string in SEC fromat is expected."); 
		}
		
		if (!prefix4 && length != 66) {
			throw new IllegalArgumentException("A valid string in SEC fromat is expected."); 
		}
		
		Secp256k1Element x = 
				new Secp256k1Element(new BigInteger(sec.substring(2,  66), 16));
		
		if (prefix4) {  // Uncompressed format ...			
			Secp256k1Element y = 
					new Secp256k1Element(new BigInteger(sec.substring(66), 16));
			
			return fromCurve(x, y); 
		}
		
		// Handle compressed format ...
		Secp256k1Element ySquare = 
				new Secp256k1Element(G.xCubePlusAxPlusB(x).value());
		
		Secp256k1Element y1 = ySquare.sqrt();
		Secp256k1Element y2 = new Secp256k1Element(y1.order().subtract(y1.value()));
		
		boolean odd = isOdd(y1.value());
		// If odd is what we want based on the prefix ...
		if (prefix3) {
			if (odd) {
				return fromCurve(x, y1); 
			}
			
			return  fromCurve(x, y2); 
		}
		
		// At this point even is what we want based on the prefix ...
		if (odd) {
			return fromCurve(x, y2); 
		}
		
		return  fromCurve(x, y1); 
	}
	
	public static Secp256k1Point fromCurve(Secp256k1Element x, Secp256k1Element y) {
		checkNull(x, y);
		
		return new Secp256k1Point(x, y);
	}
	
	public static Secp256k1Point identity() {
		if (identity == null) {
			identity = new Secp256k1Point();
		}
		
		return identity;
	}
}
