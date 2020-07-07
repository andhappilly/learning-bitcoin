package bitcoin.network;

import static bitcoin.util.BigInt.from;
import static bitcoin.util.BigInt.fromLittleEndian;
import static bitcoin.util.BigInt.toLittleEndian;
import static bitcoin.util.Bytes.EMPTY_BYTES;
import static bitcoin.util.Bytes.areEqual;
import static bitcoin.util.Bytes.bytesToHex;
import static bitcoin.util.Bytes.hexToBytes;
import static bitcoin.util.Bytes.newBytes;
import static bitcoin.util.Crypto.hash256;
import static bitcoin.util.Functions.checkNull;
import static bitcoin.util.Functions.isNull;

import java.util.Arrays;

import bitcoin.util.BytesEncodeable;
import bitcoin.util.InputSource;
import bitcoin.util.OutputSink;

public final class NetworkEnvelope extends BytesEncodeable {
	private static final byte[] MAINNET_MAGIC = new byte[] {
			(byte)0xf9, (byte)0xbe, (byte)0xb4, (byte)0xd9
	};
	
	private static final byte[] TESTNET_MAGIC = new byte[] {
			(byte)0x0b, (byte)0x11, (byte)0x09, (byte)0x07
	};
	
	private String msgType;
	private byte[] payload;
	private boolean testnet;
	
	private transient volatile String rep;
	
	public NetworkEnvelope(String msgType, byte[] payload) {
		this(msgType, payload, false);
	}
	
	public NetworkEnvelope(String msgType, byte[] payload, boolean testnet) {
		checkNull(msgType, payload);
		
		this.msgType = msgType;
		this.payload = payload;
		this.testnet = testnet;
	}
	
	public String getMsgType() {
		return msgType;
	}

	public byte[] getPayload() {
		return payload;
	}
	
	public String toString() {
		if (isNull(rep)) {
			String payloadHex = bytesToHex(payload);
			
			StringBuilder buffer = 
					new StringBuilder(msgType.length() + payloadHex.length() + 2);
			
			buffer.append(msgType).append(": ").append(payloadHex);
			rep = buffer.toString();
		}
		
		return rep;
	}

	public void writeTo(OutputSink sink) {
		checkNull(sink);
		
		// First write the magic bytes ...
		sink.write(testnet ? TESTNET_MAGIC : MAINNET_MAGIC);
		
		//Next write the command bytes ...
		sink.write(msgType.getBytes());
		
		// Next write the payload length as a little endian ...
		byte[] bytes = toLittleEndian(from(payload.length), 4);
		sink.write(bytes);
		
		// Next write the checksum bytes (first 4 bytes) ...
		bytes = newBytes(hash256(payload), 0, 4);
		sink.write(bytes);
		
		// Finally write the payload bytes ...
		sink.write(payload);
	}
	
	public static NetworkEnvelope parse(InputSource source) {
		checkNull(source);
		
		// Read the magic bytes (4 bytes or 8 characters) first ...
		char[] chars = source.readNextChars(8);
		byte[] bytes = hexToBytes(chars);
		
		boolean testnet = Arrays.equals(bytes, TESTNET_MAGIC);
		if (!testnet) {
			if (Arrays.equals(bytes, MAINNET_MAGIC)) {
				testnet = false;
			} else {
				throw new IllegalArgumentException("Envelope not valid.");
			}
		}
		
		// Next read the command bytes (12 bytes or 24 characters) ... 
		chars = source.readNextChars(24);	
		String command = new String(hexToBytes(chars));
		
		// Next read the payload length (4 bytes or 8 characters in little-endian) ...
		chars = source.readNextChars(8);
		bytes = hexToBytes(chars);
		int payloadLength = fromLittleEndian(bytes).intValue();
		
		// Next read the payload checksum (4 bytes or 8 characters) ...
		chars = source.readNextChars(8);
		byte[] checksum = hexToBytes(chars);
		
		byte[] payload = EMPTY_BYTES;
		if (payloadLength != 0) {					
			// Next read the payload (payloadLength * 2 characters) ...
			chars = source.readNextChars(payloadLength * 2);
			payload = hexToBytes(chars);			
		}
		
		// Check first 4 bytes of calculated checksum matches the given checksum ...
		byte[] calculatedChecksum = hash256(payload);
		
		if (!areEqual(checksum, 0, checksum.length, calculatedChecksum, 0, 4)) {
			throw new IllegalArgumentException("Envelope not valid.");
		}
		
		return new NetworkEnvelope(command, payload, testnet);
	}
}
