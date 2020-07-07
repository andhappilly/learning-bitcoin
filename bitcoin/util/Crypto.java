package bitcoin.util;

import static bitcoin.util.Functions.checkNull;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public final class Crypto {
	static {
		Security.addProvider(new BouncyCastleProvider());
	}
	
	private Crypto() {}
	
	public static byte[] sha1(byte[] content) {
		return digest(content, "SHA-1", null);
	}
	
	public static byte[] sha256(byte[] content) {
		return digest(content, "SHA-256", null);
	}
	
	public static byte[] hash256(byte[] content) {
		// hashing done 2 times to minimize problems such as "birthday attacks" ... 
		return digest(digest(content, "SHA-256", null), "SHA-256", null);
	}
	
	public static byte[] hash160(byte[] content) {
		return digest(sha256(content), "RipeMD160", BouncyCastleProvider.PROVIDER_NAME);
	}
	
	public static BigInteger sha256BigInt(byte[] content) {
		byte[] hash = sha256(content);
		// Use '1' to indicate an unsigned value ...
		return new BigInteger(1, hash);
	}
	
	public static BigInteger hash256BigInt(byte[] content) {
		byte[] hash = hash256(content);
		// Use '1' to indicate an unsigned value ...
		return new BigInteger(1, hash);
	}
	
	public static byte[] hmac256(byte[] secret, byte[] content) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(secret, "HmacSHA256"));
			return mac.doFinal(content);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}		
	}
	
	private static byte[] digest(byte[] content, String algorithm, String provider) {
		checkNull(content);
		
		MessageDigest digestor;
		try {
			if (provider == null) {
				digestor = MessageDigest.getInstance(algorithm);
			} else {
				digestor = MessageDigest.getInstance(algorithm, provider);
			}
		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			throw new RuntimeException(e);
		}
		
		return digestor.digest(content);
	}
}
