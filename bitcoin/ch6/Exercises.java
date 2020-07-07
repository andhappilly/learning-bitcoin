package ch6;

import static bitcoin.util.Bytes.hexToBytes;

import java.util.Arrays;

import bitcoin.lang.Script;
import bitcoin.lang.dtype.HexString;
import bitcoin.lang.dtype.IntValue;
import bitcoin.util.InputSource;
import misc.ExercisesUtil;

public final class Exercises {
	
	private Exercises() {}
	
	public static void main(String[] args) {
		// Make sure all the opcodes are loaded ...
		ExercisesUtil.loadOpCodes();
				
		System.out.println("***** Exercise 3 *****");
		String s = "767695935687";
		
		Script script1 = Script.parse(InputSource.wrap(hexToBytes(s)));
		Script script2 = new Script(Arrays.asList(new IntValue(2)));
		Script script = script1.add(script2);
		// Verify that integer value '2' unlocks this script ...
		//System.out.println(script.evaluate(new IntValue(2)));
		System.out.println(script.evaluate());
		System.out.println();
		
		System.out.println("***** Exercise 4 *****");
		// Given 2 distinct string values, the following example finds the hashes to be the same (expect 'true')
		// when the sha-1 hashing is used (from Google's security project example)...
		s = "6e879169a77ca787";
		script = Script.parse(InputSource.wrap(hexToBytes(s)));
		
		HexString a = new HexString("255044462d312e330a25e2e3cfd30a0a0a312030206f626a0a3c3c2f576964746820" + 
				"32203020522f4865696768742033203020522f547970652034203020522f537562747970652035" + 
				"203020522f46696c7465722036203020522f436f6c6f7253706163652037203020522f4c656e67" + 
				"74682038203020522f42697473506572436f6d706f6e656e7420383e3e0a73747265616d0affd8" + 
				"fffe00245348412d3120697320646561642121212121852fec092339759c39b1a1c63c4c97e1ff" + 
				"fe017f46dc93a6b67e013b029aaa1db2560b45ca67d688c7f84b8c4c791fe02b3df614f86db169" + 
				"0901c56b45c1530afedfb76038e972722fe7ad728f0e4904e046c230570fe9d41398abe12ef5bc" + 
				"942be33542a4802d98b5d70f2a332ec37fac3514e74ddc0f2cc1a874cd0c78305a215664613097" + 
				"89606bd0bf3f98cda8044629a1");
		
		HexString b = new HexString("255044462d312e330a25e2e3cfd30a0a0a312030206f626a0a3c3c2f576964746820" + 
				"32203020522f4865696768742033203020522f547970652034203020522f537562747970652035" + 
				"203020522f46696c7465722036203020522f436f6c6f7253706163652037203020522f4c656e67" + 
				"74682038203020522f42697473506572436f6d706f6e656e7420383e3e0a73747265616d0affd8" + 
				"fffe00245348412d3120697320646561642121212121852fec092339759c39b1a1c63c4c97e1ff" + 
				"fe017346dc9166b67e118f029ab621b2560ff9ca67cca8c7f85ba84c79030c2b3de218f86db3a9" + 
				"0901d5df45c14f26fedfb3dc38e96ac22fe7bd728f0e45bce046d23c570feb141398bb552ef5a0" + 
				"a82be331fea48037b8b5d71f0e332edf93ac3500eb4ddc0decc1a864790c782c76215660dd3097" + 
				"91d06bd0af3f98cda4bc4629b1");
		
		System.out.println(script.evaluate(a, b));
		System.out.println();
	}
}
