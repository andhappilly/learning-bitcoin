package bitcoin.lang.script;

import static bitcoin.util.Bytes.hexToBytes;
import static bitcoin.util.Coder.encodeToBase58WithChecksum;
import static bitcoin.util.Functions.checkNull;

public final class P2SH {
	private P2SH() {}
	
	public static String addressFor(String scriptHex, boolean testnet) {
		checkNull(scriptHex);
		
		return encodeToBase58WithChecksum(
				testnet ? (byte)0x04 : (byte)0x05,hexToBytes(scriptHex));
	}
}
