package bitcoin.network.message;

import static bitcoin.util.BigInt.toLittleEndian;
import static bitcoin.util.Bytes.hexToBytes;
import static bitcoin.util.Functions.checkNull;

import java.math.BigInteger;
import java.util.Random;

import bitcoin.util.InputSource;

public final class Ping extends PingPong {
	public static final String TYPE = "ping";
	
	static {
		register(Ping.class, TYPE);
	}
	
	public Ping() {
		this(toLittleEndian(new BigInteger(512, new Random()), 8));
	}
	
	private Ping(byte[] nonce) {
		super(nonce, TYPE);
	}
	
	public static Ping parse(InputSource source) {
		checkNull(source);
		
		// First read the version 
		char[] chars = source.readNextChars(16);
		byte[] nonce = hexToBytes(chars);
		return new Ping(nonce);
	}
}
