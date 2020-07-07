package bitcoin.crypto.ecc;

import static bitcoin.crypto.ecc.Secp256k1.G;
import static bitcoin.crypto.ecc.Secp256k1.N;
import static bitcoin.util.BigInt.isGreaterThanZero;
import static bitcoin.util.BigInt.isThisGreaterThanThat;
import static bitcoin.util.BigInt.isThisLessThanThat;
import static bitcoin.util.Bytes.ONE;
import static bitcoin.util.Bytes.ZERO;
import static bitcoin.util.Bytes.areEqual;
import static bitcoin.util.Bytes.base58ToBytes;
import static bitcoin.util.Bytes.bytesToBase58;
import static bitcoin.util.Bytes.bytesToHex;
import static bitcoin.util.Bytes.concatenate;
import static bitcoin.util.Bytes.get;
import static bitcoin.util.Bytes.ltrimIndex;
import static bitcoin.util.Bytes.ltrimFitToSizeIndex;
import static bitcoin.util.Bytes.newBytes;
import static bitcoin.util.Bytes.stitch;
import static bitcoin.util.Bytes.suffix;
import static bitcoin.util.Bytes.zeroPrefix;
import static bitcoin.util.Crypto.hash256;
import static bitcoin.util.Crypto.hmac256;
import static bitcoin.util.Functions.checkNull;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import bitcoin.util.Bytes;
import bitcoin.util.Crypto;

public final class Secret {		
	private static final ThreadLocal<SecureRandom> RANDOM_PROVIDER = 
		new ThreadLocal<SecureRandom>() {
            @Override protected SecureRandom initialValue() {
                try {
					return SecureRandom.getInstanceStrong();
				} catch (NoSuchAlgorithmException e) {
					throw new RuntimeException(e);
				}
        }
	};
	
	private static final byte[] ZERO_BYTE_ARRAY = new byte[] {0};
	private static final byte[] ONE_BYTE_ARRAY = new byte[] {1};
	
	private static final byte TESTNET_PREFIX = (byte)0xef; 
	private static final byte MAINNET_PREFIX = (byte)0x80;
	
	private static final byte COMPRESSED_SUFFIX = (byte)0x01;
	
	private BigInteger key;
	private Secp256k1Point shared;
	
	public Secret(BigInteger key) {
		checkNull(key);
		
		this.key = key;
		this.shared = G.multiply(key);
	}
	
	public Secp256k1Point pubKey() {
		return shared;
	}
	
	public Signature sign(BigInteger z) {
		checkNull(z);
		// Use '1' to indicate we want only unsigned value ...
		BigInteger k = new BigInteger(1, getRandomBytes());
		return sign(z, k);
	}
	
	public Signature sign(BigInteger z, BigInteger k) {
		BigInteger r = G.addSelf(k).getX().value();
		// The formula for the inverse uses Fermat's little theorem, that is
		// k ^ -1 = k ^ -2 % N, since this is a finite group of order N ...
		BigInteger kInverse = k.modPow(N.subtract(BigInteger.TWO), N);
		
		// The following makes use of the fact that:
		// (z + er)/k % N = s ...
		// Note that we use 'this' point as the public key in the calculation ...
		BigInteger s = z.add(r.multiply(key)).multiply(kInverse).mod(N);
		BigInteger midN = N.divide(BigInteger.TWO);
		s = isThisGreaterThanThat(s, midN) ? N.subtract(s) : s;
		
		return new Signature(r, s);
	}
	
	public String toWIF() {
		return toWIF(true, true);
	}
	
	public String toWIF(boolean compressed) {
		return toWIF(compressed, true);
	}
	
	public String toWIF(boolean compressed, boolean testnet) {
		// Get the original bytes of the secret ...
		byte[] kBytes = key.toByteArray();
		int kStart = ltrimFitToSizeIndex(kBytes, 32);
		// Check if prefix padding required ...
		if (kStart < 0) {
			// Add zero prefix padding ...
			kBytes = zeroPrefix(-kStart, kBytes);
		}
		
		// Create modified bytes with a prefix and suffix added ...
		byte prefix = testnet ? TESTNET_PREFIX : MAINNET_PREFIX;		
		if (compressed) {
			byte suffix = COMPRESSED_SUFFIX;
			kBytes = stitch(prefix, kBytes, suffix);
		} else {
			kBytes = stitch(prefix, kBytes);
		}
		
		// Calculate the checksum of the bytes ...
		byte[] c = hash256(kBytes);
		
		// Add the first 4 bytes of the checksum to the bytes ...
		kBytes = suffix(kBytes, c, 0, 4);
		
		// Finally calculate the base58 of the whole ...
		return bytesToBase58(kBytes);
	}
	
	public static Secret fromWIF(String b58) {
		checkNull(b58);
		
		byte[] b = base58ToBytes(b58);
		int nonZeroIndex = ltrimIndex(b);		
		if (nonZeroIndex < 0 || 
				(b[nonZeroIndex] != TESTNET_PREFIX && b[nonZeroIndex] != MAINNET_PREFIX)) {
			throw new IllegalArgumentException("Invalid base58");
		}
		
		byte[] bWithPrefix = newBytes(b, nonZeroIndex, b.length - 4);
		byte[] calculatedChecksum = hash256(bWithPrefix);
		
		// We compare the last 4 bytes of the given b58 array representing the attached
		// checksum to the first 4 bytes of the calculated checksum ...
		if (!areEqual(b, b.length - 4, b.length, calculatedChecksum, 0, 4)) {
			throw new IllegalArgumentException("Address not in valid format.");
		}
		
		// Check if compression flag is there ...
		boolean compFlag = b[b.length - 5] == COMPRESSED_SUFFIX;
		
		// The actual key bytes are the middle 20 bytes ... we discard the first byte
		// that represents the network marker (testnet or mainnet) and the last 4 bytes
		// that are the checksum ...
		byte[] keyBytes = newBytes(b, nonZeroIndex + 1, b.length - (compFlag ? 5 : 4));
		BigInteger key = new BigInteger(1, keyBytes);
		
		return new Secret(key);
	}
	
	private byte[] getRandomBytes() {
		SecureRandom random = RANDOM_PROVIDER.get();
		byte[] bytes = new byte[32];
		random.nextBytes(bytes);
		
		return bytes;
	}
	
	private BigInteger getDeterministicK(BigInteger z) {		
		byte[] k = get(32, ZERO);
		byte[] v = get(32, ONE);
		
		System.out.println("k = "+bytesToHex(k));
		System.out.println("v = "+bytesToHex(v));
		
		z = isThisGreaterThanThat(z, N) ? N.subtract(z) : z;
		
		// Convert to octet(8) string before transforming to bytes ...
		System.out.println("z="+z.toString(16));
		System.out.println("x="+key.toString(16));
		
		byte[] zBytes = z.toByteArray(); 	
		int index = ltrimFitToSizeIndex(zBytes, 32);
		// Check if prefix padding required ...
		if (index < 0) {
			// Add zero prefix padding ...
			zBytes = zeroPrefix(-index, zBytes);
		}
		
		byte[] eBytes = key.toByteArray();
		index = ltrimFitToSizeIndex(eBytes, 32);
		// Check if prefix padding required ...
		if (index < 0) {
			// Add zero prefix padding ...
			eBytes = zeroPrefix(-index, eBytes);
		}
		
		byte[] m = concatenate(v, ZERO_BYTE_ARRAY, eBytes, zBytes);		
		
		k = hmac256(k, m);
		v = hmac256(k, v);
		
		System.out.println("k = "+bytesToHex(k));
		System.out.println("v = "+bytesToHex(v));
		
		m = concatenate(v, ONE_BYTE_ARRAY, eBytes, zBytes);
		
		k = hmac256(k, m);
		v = hmac256(k, v);
		
		System.out.println("k = "+bytesToHex(k));
		System.out.println("v = "+bytesToHex(v));
		
		while (true) {
			v = hmac256(k, v);
			
			System.out.println("k = "+bytesToHex(k));
			System.out.println("v = "+bytesToHex(v));
			
			BigInteger candidate = new BigInteger(1, v);
			if (isGreaterThanZero(candidate) &&
					isThisLessThanThat(candidate, N)) {
				return candidate;
			}
			
			m = stitch(v, ZERO);
			
			k = hmac256(k, m);
			v = hmac256(k, v);
			
			System.out.println("k = "+bytesToHex(k));
			System.out.println("v = "+bytesToHex(v));
		}		
	}
	
	public static void main(String[] args) {
		String m = "sample";
		System.out.println("h(m) = "+Bytes.bytesToHex(Crypto.sha256(m.getBytes())));
		BigInteger e = new BigInteger("09A4D6792295A7F730FC3F2B49CBC0F62E862272F", 16);
		BigInteger z = Crypto.sha256BigInt(m.getBytes());
		System.out.println("z = "+z.toString(16));
		Secret secret = new Secret(e);
		BigInteger k = secret.getDeterministicK(z);
		//BigInteger k = secret.getDeterministicK(new BigInteger(1, m.getBytes()));
		System.out.println(k.toString(16));
		
		byte[] one = BigInteger.ONE.toByteArray();
		System.out.println("----- One size = "+one.length);
		
		String b58 = "cMpito6q7BH1DVCVZWSsDAe9tzms9rNQjteYQgwEJtvw1mqTpyE4";
		secret = fromWIF(b58);
	}
}
