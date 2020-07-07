package bitcoin.util;

import static bitcoin.util.Bytes.areEqual;
import static bitcoin.util.Bytes.base58ToBytes;
import static bitcoin.util.Bytes.bytesToBase58;
import static bitcoin.util.Bytes.newBytes;
import static bitcoin.util.Bytes.stitch;
import static bitcoin.util.Bytes.suffix;
import static bitcoin.util.Crypto.hash160;
import static bitcoin.util.Crypto.hash256;
import static bitcoin.util.Functions.checkNull;

public final class Coder {
	private Coder() {}
	
	public static String encodeToBase58WithChecksum(byte networkMarker, byte[] data) {
		checkNull(data);
		
		// Get the hash of the data bytes ...
		byte[] h = hash160(data);
		
		// Prefix the network marker ...
		h = stitch(networkMarker, h);
		
		// Calculate the checksum of the hash plus marker ...
		byte[] c = hash256(h);
		
		// Add the first 4 bytes of the checksum to the hash ...
		h = suffix(h, c, 0, 4);
		
		// Finally calculate the base58 of the whole ...
		return bytesToBase58(h);
	}
	
	public static byte[] decodeFromBase58(String b58) {
		checkNull(b58);
		
		byte[] b = base58ToBytes(b58);
		int diff = b.length - 25;
		
		if (diff < 0) {
			throw new IllegalArgumentException("Address not in valid format.");
		}
		
		byte[] addressWithNetworkPrefix = newBytes(b, diff, b.length - 4);
		byte[] calculatedChecksum = hash256(addressWithNetworkPrefix);
		
		// We compare the last 4 bytes of the given b58 array representing the attached
		// checksum to the first 4 bytes of the calculated checksum ...
		if (!areEqual(b, b.length - 4, b.length, calculatedChecksum, 0, 4)) {
			throw new IllegalArgumentException("Address not in valid format.");
		}
		
		// The actual address bytes are the middle 20 bytes ... we discard the first byte
		// that represents the network marker (testnet or mainnet) and the last 4 bytes
		// that are the checksum ...
		return newBytes(b, diff + 1, b.length - 4);
	}
}
