package bitcoin.network.message;

import static bitcoin.util.BigInt.from;
import static bitcoin.util.BigInt.fromLittleEndian;
import static bitcoin.util.BigInt.fromVarInt;
import static bitcoin.util.BigInt.getVarIntSize;
import static bitcoin.util.BigInt.toLittleEndian;
import static bitcoin.util.BigInt.toVarInt;
import static bitcoin.util.Bytes.EMPTY_BYTES;
import static bitcoin.util.Bytes.ZERO;
import static bitcoin.util.Bytes.bytesToInt;
import static bitcoin.util.Bytes.hexCharsToByte;
import static bitcoin.util.Bytes.hexToBytes;
import static bitcoin.util.Bytes.intToBytes;
import static bitcoin.util.Bytes.newBytes;
import static bitcoin.util.Functions.areEqual;
import static bitcoin.util.Functions.checkNull;
import static bitcoin.util.Functions.isNull;

import java.math.BigInteger;
import java.util.Random;

import bitcoin.network.Message;
import bitcoin.util.InputSource;
import bitcoin.util.OutputSink;

public final class Version extends Message {
	public static final String TYPE = "version";
	
	private static final BigInteger DEFAULT_VERSION = from(70015);
	private static final BigInteger DEFAULT_SERVICES = BigInteger.ZERO;
	private static final byte[] DEFAULT_IP = newBytes(16, (byte)0);
	private static final int DEFAULT_PORT = 8333;
	private static final BigInteger DEFAULT_HEIGHT = BigInteger.ZERO;
	//private static final byte[] DEFAULT_USER_AGENT = "ProgrammingBitcoin:101".getBytes();
	private static final byte[] DEFAULT_USER_AGENT = "programmingbitcoin:0.1".getBytes();
	
	static {
		register(Version.class, TYPE);
	}
	
	private BigInteger version;
	private BigInteger services;
	private BigInteger timestamp;
	private BigInteger receiverServices;
	private byte[] receiverIP;
	private int recieverPort;
	private BigInteger senderServices;
	private byte[] senderIP;
	private int senderPort; 
	private byte[] nonce;
	private byte[] userAgent;
	private BigInteger height;
	private boolean relay;
	
	public Version() {
		this(DEFAULT_VERSION, DEFAULT_SERVICES, from(System.currentTimeMillis()), DEFAULT_SERVICES,
				DEFAULT_IP, DEFAULT_PORT, DEFAULT_SERVICES, DEFAULT_IP, DEFAULT_PORT,
				toLittleEndian(new BigInteger(512, new Random()), 8), DEFAULT_USER_AGENT, DEFAULT_HEIGHT, false);
	}
	
	public Version(BigInteger version, BigInteger services, BigInteger timestamp, BigInteger receiverServices,
			byte[] receiverIP, int recieverPort, BigInteger senderServices, byte[] senderIP, int senderPort,
			byte[] nonce, byte[] userAgent, BigInteger height, boolean relay) {
		
		super(TYPE);
		
		checkNull(version, services, timestamp, receiverServices, 
				receiverIP, senderServices, senderIP, nonce, userAgent, height);
		
		this.version = version;
		this.services = services;
		this.timestamp = timestamp;
		this.receiverServices = receiverServices;
		this.receiverIP = receiverIP;
		this.recieverPort = recieverPort;
		this.senderServices = senderServices;
		this.senderIP = senderIP;
		this.senderPort = senderPort;
		this.nonce = nonce;
		this.userAgent = userAgent;
		this.height = height;
		this.relay = relay;
	}

	public void writeTo(OutputSink sink) {
		checkNull(sink);
		
		// First write the version bytes ...
		byte[] bytes = toLittleEndian(version, 4);
		sink.write(bytes);
		
		// Next write the services bytes ...
		bytes = toLittleEndian(services, 8);
		sink.write(bytes);
		
		// Next write the timestamp bytes ...
		bytes = toLittleEndian(timestamp, 8);
		sink.write(bytes);
		
		// Next write the receiver services bytes ...
		bytes = toLittleEndian(receiverServices, 8);
		sink.write(bytes);
		
		// Next write the reciever ip bytes ...
		sink.write(receiverIP);
		
		// Next write the receiver port bytes ...
		bytes = intToBytes(recieverPort);
		bytes = newBytes(bytes, bytes.length - 2, bytes.length);
		sink.write(bytes);
		
		// Next write the sender services bytes ...
		bytes = toLittleEndian(senderServices, 8);
		sink.write(bytes);
		
		// Next write the sender ip bytes ...
		sink.write(senderIP);
		
		// Next write the sender port bytes ...
		bytes = intToBytes(senderPort);
		bytes = newBytes(bytes, bytes.length - 2, bytes.length);
		sink.write(bytes);
		
		// Next write the nonce bytes ...
		sink.write(nonce);
		
		// Next write the user agent bytes ...
		if (isNull(userAgent) || userAgent.length == 0) {
			sink.write(ZERO);
		} else {
			byte[] userAgentLength = toVarInt(from(userAgent.length));
			
			sink.write(userAgentLength);
			sink.write(userAgent);
		}
		
		// Next write the height bytes ...
		bytes = toLittleEndian(height, 4);
		sink.write(bytes);
		
		if (relay) {
			sink.write((byte)1);
		}
	}	
	
	public static Version parse(InputSource source) {
		checkNull(source);
		
		// First read the version 
		char[] chars = source.readNextChars(8);
		byte[] bytes = hexToBytes(chars);
		BigInteger version = fromLittleEndian(bytes);
		
		// Next read services ...
		chars = source.readNextChars(16);
		bytes = hexToBytes(chars);
		BigInteger services = fromLittleEndian(bytes);
		
		// Next read timestamp ...
		chars = source.readNextChars(16);
		bytes = hexToBytes(chars);
		BigInteger timestamp = fromLittleEndian(bytes);
		
		// Next read receiver services ...
		chars = source.readNextChars(16);
		bytes = hexToBytes(chars);
		BigInteger receiverServices = fromLittleEndian(bytes);
		
		// Next read the receiver ip ...
		chars = source.readNextChars(32);
		byte[] receiverIP = hexToBytes(chars);
		
		// Next read the receiver port ...
		chars = source.readNextChars(4);
		bytes = hexToBytes(chars);
		int receiverPort = bytesToInt(bytes);
		
		// Next read sender services ...
		chars = source.readNextChars(16);
		bytes = hexToBytes(chars);
		BigInteger senderServices = fromLittleEndian(bytes);
		
		// Next read the sender ip ...
		chars = source.readNextChars(32);
		byte[] senderIP = hexToBytes(chars);
		
		// Next read the sender port ...
		chars = source.readNextChars(4);
		bytes = hexToBytes(chars);
		int senderPort = bytesToInt(bytes);		
		
		// Next read nonce ...
		chars = source.readNextChars(16);
		byte[] nonce = hexToBytes(chars);		
		
		// The "user agent" field is of variable length, hence need to read the next byte
		// to determine the size ..
		chars = source.readNextChars(2);
		byte b = hexCharsToByte(chars[0], chars[1]);
		int vIntSize = getVarIntSize(b);		
		
		BigInteger userAgentSize;
		if (vIntSize == 1) {
			// If the variable integer size is 1, the already read byte represents the size ...
			userAgentSize = BigInteger.valueOf(b);
		} else {
			// Read the next 2*vIntSize characters that represent the size ...
			chars = source.readNextChars(2*vIntSize);
			bytes = hexToBytes(chars);
			userAgentSize = fromVarInt(bytes);
		}
		
		byte[] userAgent;
		if (areEqual(BigInteger.ZERO, userAgentSize)) {
			userAgent = EMPTY_BYTES;
		} else {
			// Read the next 2*userAgentSize characters that represent 'useragent' hex ...
			chars = source.readNextChars(BigInteger.TWO.multiply(userAgentSize).intValue());
			userAgent = hexToBytes(chars);
		}
		
		// Next read the height field ...
		chars = source.readNextChars(8);
		bytes = hexToBytes(chars);
		BigInteger height = fromLittleEndian(bytes);		
		
		// Finally read the relay flag if it exists ...
		char relayFlag = source.readNextChar(false);
		boolean relay = (relayFlag & 1) == 1;
		
		return new Version(version, services, timestamp, receiverServices, receiverIP, 
				receiverPort, senderServices, senderIP, senderPort, nonce, userAgent, height, relay);
	}
	
	public static void main(String[] args) {
		Version vm = new Version();
	}
}
