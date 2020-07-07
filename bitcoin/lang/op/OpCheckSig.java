package bitcoin.lang.op;

import static bitcoin.util.Bytes.bytesToHex;

import java.math.BigInteger;
import java.util.Stack;

import bitcoin.crypto.ecc.Secp256k1Point;
import bitcoin.crypto.ecc.Signature;
import bitcoin.lang.dtype.Data;
import bitcoin.lang.dtype.HexString;
import bitcoin.lang.dtype.IntValue;
import bitcoin.lang.dtype.NormalString;

public final class OpCheckSig extends OpCode {
	public static final OpCheckSig INSTANCE = new OpCheckSig();
	
	private OpCheckSig() {
		register(172,  this);
	}
	
	public String getName() {
		return "OP_CHECKSIG";
	}
	
	protected boolean exec(Context context) {
		Stack<Data<?>> inputs = context.getRegularStack();
		if (inputs.size() < 3) {
			return false;
		}
		
		Secp256k1Point pubKey;
		try {
			pubKey = parsePublicKey(inputs.pop());
		} catch(Exception e) {
			return false;
		}
		
		Signature signature;
		try {
			signature = parseSignature(inputs.pop());
		} catch(Exception e) {
			return false;
		}
		
		BigInteger z;
		try {
			z = parseZ(inputs.pop());
		} catch(Exception e) {
			return false;
		}	
		
		boolean verified = pubKey.verifySignature(z, signature);
		inputs.push(verified ? IntValue.SUCCESS : NormalString.FAILURE);
		
		return verified;
	}
	
	public byte toByte() {
		return (byte)172; 
	}
	
	private Secp256k1Point parsePublicKey(Data<?> data) {
		if (data instanceof HexString) {
			HexString hex = (HexString)data;
			return Secp256k1Point.fromSEC(hex.read());
		}
		
		String hex = bytesToHex(data.readAsBytes());
		return Secp256k1Point.fromSEC(hex);
	}
	
	private Signature parseSignature(Data<?> data) {
		if (data instanceof HexString) {
			HexString hex = (HexString)data;
			return Signature.fromDER(hex.read());
		}
		
		String hex = bytesToHex(data.readAsBytes());
		return Signature.fromDER(hex);
	}
	
	private BigInteger parseZ(Data<?> data) {
		if (data instanceof HexString) {
			HexString hex = (HexString)data;
			//return new BigInteger(hex.consume(), 16);
			return new BigInteger(1, hex.readAsBytes());
		}
		
		String hex = bytesToHex(data.readAsBytes());
		return new BigInteger(hex, 16);
	}
}
