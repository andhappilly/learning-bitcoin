package bitcoin.core;

import static bitcoin.util.BigInt.from;
import static bitcoin.util.Bytes.ltrimIndex;
import static bitcoin.util.Bytes.newBytes;
import static bitcoin.util.Bytes.reverse;
import static bitcoin.util.Bytes.stitch;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

public final class PowHelper {
	private static final BigInteger TWO_FIFTY_SIX = from(256);
	private static final BigDecimal BASELINE_TARGET = 
			new BigDecimal(new BigInteger("ffff", 16).multiply(TWO_FIFTY_SIX.pow(26)));
	
	private static final BigDecimal FOUR = new BigDecimal(from(4));
	private static final BigDecimal TWO_WEEK_SECONDS = new BigDecimal(from(60*60*24*14));
	private static final BigDecimal MAX_PERIOD = TWO_WEEK_SECONDS.multiply(FOUR);
	private static final BigDecimal MIN_PERIOD = TWO_WEEK_SECONDS.divide(FOUR, MathContext.DECIMAL128);
	
	private PowHelper() {}
	
	public static BigInteger bitsToTarget(BigInteger bits) {
		byte[] bytes = bits.toByteArray();
		
		// We need the little-endian representation, hence reverse the bytes ...
		reverse(bytes);
		
		// First byte is the exponent ...
		int exponent = Byte.toUnsignedInt(bytes[0]);
		
		// Next 3 bytes represent the coefficient ...
		BigInteger coefficient = new BigInteger(1, newBytes(bytes, 1, 4));
		
		// Now return the target ...
		return coefficient.multiply(TWO_FIFTY_SIX.pow(exponent - 3));
	}
	
	public static BigInteger targetToBits(BigInteger target) {
		System.out.println("----- target = "+target.toString(16));
		byte[] bytes = target.toByteArray();
		int index = ltrimIndex(bytes);
		
		int exponent;
		byte[] coefficient;
		if (bytes[index] > 0x7f) {
			exponent = bytes.length - index + 1;
			coefficient = stitch((byte)0, bytes, index, index + 2);
		} else {
			exponent = bytes.length - index;
			coefficient = newBytes(bytes, index, index + 3);
		}
		
		reverse(coefficient);
		
		byte[] bits = stitch(coefficient, (byte)exponent);
		return new BigInteger(1, bits);
	}
	
	public static BigDecimal difficultyFromTarget(BigInteger target) {
		return BASELINE_TARGET.divide(new BigDecimal(target), MathContext.DECIMAL128);
	}
	
	public static BigInteger calculateNewTarget(Block firstBlock, Block lastBlock) {
		BigDecimal timeDiff = new BigDecimal(
				lastBlock.getTimestamp().subtract(firstBlock.getTimestamp()));
		if (timeDiff.compareTo(MAX_PERIOD) == 1) {
			timeDiff = MAX_PERIOD;
		}
		
		if (timeDiff.compareTo(MIN_PERIOD) == -1) {
			timeDiff = MIN_PERIOD;
		}
		
		return new BigDecimal(lastBlock.getTarget()).multiply(timeDiff).divide
							(TWO_WEEK_SECONDS, MathContext.UNLIMITED).toBigInteger();
	}
	
	public static BigInteger calculateNewBits(Block firstBlock, Block lastBlock) {		
		return targetToBits(calculateNewTarget(firstBlock, lastBlock));
	}
}
