package bitcoin.util;

import static bitcoin.util.Bytes.newBytes;
import static bitcoin.util.Functions.checkNull;

import java.math.BigInteger;
import java.util.Arrays;

public final class BloomFilter {
	public static final int BIP37_CONSTANT = 0xfba4c795;
	
	private BigInteger functionCount;
	private BigInteger tweak;
	private byte[] bitsField;
	private int numBits;
	
	private boolean modifiable;
	
	private BloomFilter(byte[] bitsField, BigInteger functionCount, BigInteger tweak) {
		checkNull(bitsField, functionCount, tweak);		

		this.functionCount = functionCount;
		this.tweak = tweak;
		this.bitsField = bitsField;
		this.numBits = bitsField.length * 8;
	}
	
	public BloomFilter(int size, BigInteger functionCount, BigInteger tweak) {	
		this(new byte[size], functionCount, tweak);
		Arrays.fill(bitsField, (byte)0);
		
		this.modifiable = true;
	}
	
	public BigInteger getFunctionCount() {
		return functionCount;
	}
	
	public BigInteger getTweak() {
		return tweak;
	}
	
	public byte[] getBitsField() {
		return newBytes(bitsField);
	}
	
	public void add(byte[] data) {
		if (!modifiable) {
			throw new UnsupportedOperationException("BloomFilter is not modifiable.");
		}
		
		int numFunctions = functionCount.intValue();
		int entropy = tweak.intValue();
		
		for (int i = 0; i < numFunctions; ++i) {
			int seed = i * BIP37_CONSTANT + entropy;
			int hash = Murmur3.of(data, seed);
			int bit = hash % numBits; // This gives the bit that needs to be set ..
			bit = bit < 0 ? numBits + bit : bit;
			
			int block = bit / 8; // This gives us the byte in which the bit exists ...
			int index = bit - block * 8; // This gives the position of the bit within the byte ...
			int marker = 1 << index;
			
			bitsField[block] = (byte)(bitsField[block] | marker);
		}
	}
	
	public static BloomFilter from(byte[] bitsField, BigInteger functionCount, BigInteger tweak) {
		BloomFilter filter = new BloomFilter(bitsField, functionCount, tweak);
		return filter;
	}
}
