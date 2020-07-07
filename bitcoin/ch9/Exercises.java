package ch9;

import java.math.BigDecimal;
import java.math.BigInteger;

import bitcoin.core.Block;
import bitcoin.core.PowHelper;
import bitcoin.util.InputSource;
import misc.ExercisesUtil;

public final class Exercises {
	private Exercises() {}
	
	public static void main(String[] args) {
		// Make sure all the opcodes are loaded ...
		ExercisesUtil.loadOpCodes();
		
		String hex = "020000208ec39428b17323fa0ddec8e887b" + 
				"4a7c53b8c0a0a220cfd0000000000000000005b0750fce0a889502d40508d39576821155e9c9e3" + 
				"f5c3157f961db38fd8b25be1e77a759e93c0118a4ffd71d";
		
		Block b = Block.parse(InputSource.wrap(hex.getBytes()));
		System.out.println(b.getId());
		System.out.println(b.isBip9Ready());
		System.out.println(b.isBip91Ready());
		System.out.println(b.isBip141Ready());		
		
		BigInteger bits = new BigInteger("e93c0118", 16);
		BigInteger target = PowHelper.bitsToTarget(bits);
		System.out.println(target.toString(16));
		System.out.println(PowHelper.targetToBits(target).toString(16));
		
		BigDecimal difficulty = PowHelper.difficultyFromTarget(target);
		System.out.println(difficulty);
		System.out.println(b.isValidPoW());
		
		String hex1 = "000000201ecd89664fd205a37" + 
				"566e694269ed76e425803003628ab010000000000000000bfcade29d080d9aae8fd461254b0418" + 
				"05ae442749f2a40100440fc0e3d5868e55019345954d80118a1721b2e";
		String hex2 = "00000020fdf740b0e49cf75bb3" + 
				"d5168fb3586f7613dcc5cd89675b0100000000000000002e37b144c0baced07eb7e7b64da916cd" +
				"3121f2427005551aeb0ec6a6402ac7d7f0e4235954d801187f5da9f5";
		
		Block first = Block.parse(InputSource.wrap(hex1.getBytes()));
		Block last = Block.parse(InputSource.wrap(hex2.getBytes()));
		target = PowHelper.calculateNewTarget(first, last);
		System.out.println(target.toString(16));
		
		// Exercise 12 ...
		System.out.println("***** Exercise 12 *****");		
		hex1 = "02000020f1472d9db4b563c35f97c428ac903f23b7fc055d1cfc26000000000000000000" + 
				"b3f449fcbe1bc4cfbcb8283a0d2c037f961a3fdf2b8bedc144973735eea707e126425859" + 
				"7e8b0118e5f00474";
		
		hex2 = "000000203471101bbda3fe307664b3283a9ef0e97d9a38a7eacd88000000000000000000" + 
				"10c8aba8479bbaa5e0848152fd3c2289ca50e1c3e58c9a4faaafbdf5803c5448ddb84559" + 
				"7e8b0118e43a81d3";
		
		first = Block.parse(InputSource.wrap(hex1.getBytes()));
		last = Block.parse(InputSource.wrap(hex2.getBytes()));
		bits = PowHelper.calculateNewBits(first, last);
		System.out.println(bits.toString(16));	
		System.out.println();
	}
}
