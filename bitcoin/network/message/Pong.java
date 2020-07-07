package bitcoin.network.message;

import static bitcoin.util.Bytes.hexToBytes;
import static bitcoin.util.Functions.checkNull;

import bitcoin.util.InputSource;

public final class Pong extends PingPong{
	public static final String TYPE = "pong";
	
	static {
		register(Pong.class, TYPE);
	}
	
	public Pong(byte[] nonce) {
		super(nonce, TYPE);
	}
	
	public static Pong parse(InputSource source) {
		checkNull(source);
		
		// First read the version 
		char[] chars = source.readNextChars(16);
		byte[] nonce = hexToBytes(chars);
		return new Pong(nonce);
	}
}
