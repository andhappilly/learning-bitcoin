package ch10;

import bitcoin.network.NetworkEnvelope;
import bitcoin.util.Bytes;
import bitcoin.util.InputSource;
import bitcoin.util.OutputSink;

public final class Exercises {
	private Exercises() {}
	
	public static void main(String[] args) {
		// Exercise 5
		System.out.println("***** Exercise 2 *****");
		String hex = "f9beb4d976657261636b000000000000000000005df6e0e2";
		NetworkEnvelope env = NetworkEnvelope.parse(InputSource.wrap(hex.getBytes()));
		System.out.println(env.getMsgType());
		System.out.println(env.getPayload().length);
		
		OutputSink sink = new OutputSink();
		env.writeTo(sink);
		System.out.println(Bytes.bytesToHex(sink.toByteArray()));
	}	
}
