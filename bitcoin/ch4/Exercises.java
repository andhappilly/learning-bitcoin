package ch4;

import static bitcoin.crypto.ecc.Secp256k1.G;
import static bitcoin.util.BigInt.from;
import static bitcoin.util.Bytes.base58ToBytes;
import static bitcoin.util.Bytes.bytesToBase58;
import static bitcoin.util.Bytes.bytesToHex;
import static bitcoin.util.Bytes.hexToBytes;

import java.math.BigInteger;

import bitcoin.crypto.ecc.Secp256k1Point;
import bitcoin.crypto.ecc.Secret;
import bitcoin.crypto.ecc.Signature;
import bitcoin.lang.script.P2PKH;

public class Exercises {
	private Exercises() {}
	
	public static void main(String[] args) {
		// Exercise 1
		System.out.println("***** Exercise 1 *****");
		BigInteger secret = from(5000); 
		Secp256k1Point publicKey = G.multiply(secret);	
		System.out.println("Uncompressed SEC of e=5000 is "+publicKey.toSEC(false));
		
		secret = from(2018).pow(5); 
		publicKey = G.multiply(secret);	
		System.out.println("Uncompressed SEC of e=2018^5 is "+publicKey.toSEC(false));
		
		secret = new BigInteger("deadbeef12345", 16); 
		publicKey = G.multiply(secret);	
		System.out.println("Uncompressed SEC of e=0xdeadbeef12345 "+publicKey.toSEC(false));		
		System.out.println();
		
		// Exercise 2
		System.out.println("***** Exercise 2 *****");
		secret = from(5001); 
		publicKey = G.multiply(secret);	
		System.out.println("Compressed SEC of e=5001 is "+publicKey.toSEC(true));
		
		secret = from(2019).pow(5); 
		publicKey = G.multiply(secret);	
		System.out.println("Compressed SEC of e=2019^5 is "+publicKey.toSEC(true));
		
		secret = new BigInteger("deadbeef54321", 16); 
		publicKey = G.multiply(secret);	
		System.out.println("Compressed SEC of e=0xdeadbeef54321 "+publicKey.toSEC(true));		
		System.out.println();
		
		// Exercise 3
		System.out.println("***** Exercise 3 *****");
		BigInteger r = new BigInteger("37206a0610995c58074999cb9767b87af4c4978db68c06e8e6e81d282047a7c6", 16); 
		BigInteger s = new BigInteger("8ca63759c1157ebeaec0d03cecca119fc9a75bf8e6d0fa65c841c8e2738cdaec", 16);
		Signature sig = new Signature(r, s);		
		System.out.println("DER is "+sig.toDER());		
		System.out.println();
		
		// Exercise 4
		System.out.println("***** Exercise 4 *****");
		
		byte[] b = hexToBytes("7c076ff316692a3d7eb3c3bb0f8b1488cf72e1afcd929e29307032997a838a3d");
		String b58 = bytesToBase58(b);
		byte[] bR = base58ToBytes(b58);
		String hex = bytesToHex(bR);
		System.out.println("Base58 for 7c076ff316692a3d7eb3c3bb0f8b1488cf72e1afcd929e29307032997a838a3d is "
								+b58 + ", recreated hex = "+hex);
		
		b = hexToBytes("eff69ef2b1bd93a66ed5219add4fb51e11a840f404876325a1e8ffe0529a2c");
		b58 = bytesToBase58(b);
		bR = base58ToBytes(b58);
		hex = bytesToHex(bR);
		System.out.println("Base58 for eff69ef2b1bd93a66ed5219add4fb51e11a840f404876325a1e8ffe0529a2c is "
								+b58 + ", recreated hex = "+hex);
		
		b = hexToBytes("c7207fee197d27c618aea621406f6bf5ef6fca38681d82b2f06fddbdce6feab6");
		b58 = bytesToBase58(b);
		bR = base58ToBytes(b58);	
		hex = bytesToHex(bR);
		System.out.println("Base58 for c7207fee197d27c618aea621406f6bf5ef6fca38681d82b2f06fddbdce6feab6 is "
								+b58 + ", recreated hex = "+hex);
		
		System.out.println();
		
		// Exercise 5
		System.out.println("***** Exercise 5 *****");
		secret = from(5002); 
		publicKey = G.multiply(secret);	
		System.out.println("Uncompressed address of e=5002 on testnet is "+P2PKH.addressFor(publicKey, false));	
		secret = from(2020).pow(5); 
		publicKey = G.multiply(secret);	
		System.out.println("Compressed address of e=2020^5 on testnet is "+P2PKH.addressFor(publicKey));
		secret = new BigInteger("12345deadbeef", 16); 
		publicKey = G.multiply(secret);	
		System.out.println("Compressed address of e=0x12345deadbeef on mainnet is "+P2PKH.addressFor(publicKey, true, false));
		System.out.println();
		
		// Exercise 6
		System.out.println("***** Exercise 6 *****");
		Secret key = new Secret(from(5003)); 
		System.out.println("Compressed WIF of e=5003 on testnet is "+key.toWIF());	
		key = new Secret(from(2021).pow(5)); 
		System.out.println("Uncompressed WIF of e=2021^5 on testnet is "+key.toWIF(false));
		key = new Secret(new BigInteger("54321deadbeef", 16)); 
		System.out.println("Compressed WIF of e=0x54321deadbeef on mainnet is "+key.toWIF(true, false));
		System.out.println();
	}
}
