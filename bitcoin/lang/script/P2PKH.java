package bitcoin.lang.script;

import static bitcoin.util.Coder.decodeFromBase58;
import static bitcoin.util.Coder.encodeToBase58WithChecksum;
import static bitcoin.util.Functions.checkNull;

import java.util.ArrayList;
import java.util.List;

import bitcoin.crypto.ecc.Secp256k1Point;
import bitcoin.lang.Command;
import bitcoin.lang.Script;
import bitcoin.lang.dtype.ByteArray;
import bitcoin.lang.op.OpCheckSig;
import bitcoin.lang.op.OpDup;
import bitcoin.lang.op.OpEqualVerify;
import bitcoin.lang.op.OpHash160;

public final class P2PKH {
	private P2PKH() {		
	}
	
	public static String addressFor(Secp256k1Point pubKey) {
		return addressFor(pubKey, true);
	}
	
	public static String addressFor(Secp256k1Point pubKey, boolean compressed) {
		return addressFor(pubKey, compressed, true);
	}
	
	public static String addressFor(Secp256k1Point pubKey, boolean compressed, boolean testnet) {
		checkNull(pubKey);
		
		return encodeToBase58WithChecksum(
				testnet ? (byte)0x6f : (byte)0x00, pubKey.toSecBytes(compressed));
	}
	
	public static Script fromAddress(String b58) {
		checkNull(b58);
		
		List<Command> commands = new ArrayList<Command>(5);
		commands.add(OpDup.INSTANCE);
		commands.add(OpHash160.INSTANCE);
		commands.add(new ByteArray(decodeFromBase58(b58)));
		commands.add(OpEqualVerify.INSTANCE);
		commands.add(OpCheckSig.INSTANCE);
		
		return new Script(commands);
	}
}
