package ch7;

import static bitcoin.util.BigInt.from;
import static bitcoin.util.BigInt.isThisEqualOrGreaterThanThat;
import static bitcoin.util.Bytes.bytesToHex;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import bitcoin.core.Transaction;
import bitcoin.core.Transaction.Input;
import bitcoin.core.Transaction.Output;
import bitcoin.crypto.ecc.Secret;
import bitcoin.lang.Script;
import bitcoin.lang.script.P2PKH;
import bitcoin.util.Bytes;
import bitcoin.util.InputSource;
import bitcoin.util.OutputSink;
import misc.ExercisesUtil;

public final class Exercises {
	private Exercises() {}
	
	public static void main(String[] args) {
		// Make sure all the opcodes are loaded ...
		ExercisesUtil.loadOpCodes();
		
		String hex = "0100000001813f79011acb80925dfe69b3def355fe914bd1d96a3f5f71bf830" + 
				"3c6a989c7d1000000006b483045022100ed81ff192e75a3fd2304004dcadb746fa5e24c5031ccf" + 
				"cf21320b0277457c98f02207a986d955c6e0cb35d446a89d3f56100f4d7f67801c31967743a9c8" + 
				"e10615bed01210349fc4e631e3624a545de3f89f5d8684c7b8138bd94bdd531d2e213bf016b278" + 
				"afeffffff02a135ef01000000001976a914bc3b654dca7e56b04dca18f2566cdaf02e8d9ada88a" + 
				"c99c39800000000001976a9141c4bc762dd5423e332166702cb75f40df79fea1288ac19430600";
		
		Transaction tx = Transaction.parse(InputSource.wrap(hex.getBytes()));
		BigInteger fee = tx.getFee();
		
		System.out.println(isThisEqualOrGreaterThanThat(fee, BigInteger.ZERO));
		
	    hex = "0100000002137c53f0fb48f83666fcfd2fe9f12d13e94ee109c5aeabbf"
				+ "a32bb9e02538f4cb000000006a47304402207e6009ad86367fc4b166bc80bf1"
				+ "0cf1e78832a01e9bb491c6d126ee8aa436cb502200e29e6dd7708ed419cd5ba"
				+ "798981c960f0cc811b24e894bff072fea8074a7c4c012103bc9e7397f739c70"
				+ "f424aa7dcce9d2e521eb228b0ccba619cd6a0b9691da796a1ffffffff517472"
				+ "e77bc29ae59a914f55211f05024556812a2dd7d8df293265acd833015901000"
				+ "0006b483045022100f4bfdb0b3185c778cf28acbaf115376352f091ad9e2722"
				+ "5e6f3f350b847579c702200d69177773cd2bb993a816a5ae08e77a6270cf46b"
				+ "33f8f79d45b0cd1244d9c4c0121031c0b0b95b522805ea9d0225b1946ecaeb1"
				+ "727c0b36c7e34165769fd8ed860bf5ffffffff027a958802000000001976a91"
				+ "4a802fc56c704ce87c42d7c92eb75e7896bdc41ae88aca5515e000000000019"
				+ "76a914e82bd75c9c662c3f5700b33fec8a676b6e9391d588ac00000000";
		
		tx = Transaction.parse(InputSource.wrap(hex.getBytes()));
		OutputSink sink = new OutputSink();
		tx.writeTo(sink);
		
		String hex2 = Bytes.bytesToHex(sink.toByteArray());
		
		System.out.println("hex2 : "+hex2);
		
		System.out.println("Hex == Hex2 := "+(hex.equals(hex2)));
		
		int numberOfInputs = tx.getInputs().size();
		System.out.println("# of inputs = "+ numberOfInputs);
		for (int i = 0; i < numberOfInputs; i++) {
			System.out.println("Input["+i+"] validity = '"+tx.verifyInput(i)+"'");
		}
		System.out.println(tx.verify());
		
		// Mock transaction construction ...
		BigInteger pvTxHash = new BigInteger(
				"0d6fe5213c0b3291f208cba8bfb59b7476dffacc4e5cb66f6eb20a080843a299", 16);
		
		BigInteger pvTxIndex = from(13);
		
		Input input = new Input(pvTxHash, pvTxIndex, null, null);		
		List<Input> inputs = Arrays.asList(input);
		
		BigInteger amount = from(33000000);
		Script script = P2PKH.fromAddress("mzx5YhAH9kNHtcN481u6WkjeHjYtVeKVh2");		
		Output output1 = new Output(amount, script);
		
		amount = from(10000000);
		script = P2PKH.fromAddress("mnrVtF8DWjMu839VW3rBfgYaAfKk8983Xf");
		Output output2 = new Output(amount, script);
		
		List<Output> outputs = Arrays.asList(output1, output2);
		
		tx = new Transaction(from(1), inputs, outputs, from(0), true, false);
		System.out.println(tx);
		//boolean signed = false;
		boolean signed = tx.signInput(0, new Secret(from(8675309)));
		
		System.out.println("signed="+signed);
		
		sink = new OutputSink();
		tx.writeTo(sink);
		String txHex = bytesToHex(sink.toByteArray());		
		System.out.println(txHex);	
		
		Transaction rTx = Transaction.parse(InputSource.wrap(txHex.getBytes()));
		System.out.println(tx.equals(rTx));
		
		txHex = "0100000001813f79011acb80925dfe69b3def355fe914bd1d96a3f5f71bf8303c6a989c7d10000" + 
				"00006a47304402207db2402a3311a3b845b038885e3dd889c08126a8570f26a844e3e4049c482a" + 
				"11022010178cdca4129eacbeab7c44648bf5ac1f9cac217cd609d216ec2ebc8d242c0a01210393" + 
				"5581e52c354cd2f484fe8ed83af7a3097005b2f9c60bff71d35bd795f54b67feffffff02a135ef" + 
				"01000000001976a914bc3b654dca7e56b04dca18f2566cdaf02e8d9ada88ac99c3980000000000" + 
				"1976a9141c4bc762dd5423e332166702cb75f40df79fea1288ac19430600";
		
		rTx = Transaction.parse(InputSource.wrap(txHex.getBytes()));
		
		System.out.println(tx.equals(rTx));
		
		
		// Exercise 5
		txHex = "02000000000101045cbaae127d7ff23644da7a06a94a8b5909b1a1b7a670cf242077e8704747bc0100000000feffffff0213493f3d0000000017a914e1eb09d3b1c9d9e6363e42e5dfe19c4251c882fa872b001f00000000001976a914ff81c2a10a348e3102901dbe7d6736d142d4804488ac024730440220107fdc5450985f5a93f8b95fffbf1feb503c5babc981cdc9e2b68572c18bc7fd02203fbf9f94c63340abb7bc3903ebbb595114abb4689d3078877cc1e5b2d2182f870121038ab29383ccafa8d575a290cb7b434bccd9a2424ddd0baa89a99d41e91e1cd9f4cda61a00";
		rTx = Transaction.parse(InputSource.wrap(txHex.getBytes()));
		System.out.println(rTx);
		//if (true) {
		//	return;
		//}
		
		pvTxHash = new BigInteger(
				"60afa92b88ed0a6edb08654d4fb6cd25814a3768621c734bac1afa18f5589dc5", 16);
		pvTxIndex = from(1);
		
		input = new Input(pvTxHash, pvTxIndex, null, null);		
		inputs = Arrays.asList(input);
		
		amount = from(1218995);
		script = P2PKH.fromAddress("mwJn1YPMq7y5F8J3LkC5Hxg9PHyZ5K4cFv");		
		output1 = new Output(amount, script);
		
		amount = from(812664);
		script = P2PKH.fromAddress("n4ox3sYwiJ7YCMqDFXpdRzTDTnqkBo5zq3");
		output2 = new Output(amount, script);
		
		outputs = Arrays.asList(output1, output2);
		
		tx = new Transaction(from(1), inputs, outputs, from(0), true, false);
		System.out.println(tx);
		
		Secret secret;
		try(BufferedReader r = new BufferedReader(
				new InputStreamReader(new FileInputStream(
						System.getProperty("user.home")+"/bitcoin/keys/secret.key")))) {
			StringBuilder buffer = new StringBuilder();
			String line = null;
			while ((line = r.readLine()) != null) {
				buffer.append(line);
			}
			
			secret = Secret.fromWIF(buffer.toString());
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
		
		signed = tx.signInput(0, secret);
		System.out.println(signed);
	}
}
