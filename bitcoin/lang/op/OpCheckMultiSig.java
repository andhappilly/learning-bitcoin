package bitcoin.lang.op;

import static bitcoin.util.Bytes.bytesToHex;
import static bitcoin.util.Bytes.bytesToInt;

import java.math.BigInteger;
import java.util.Stack;

import bitcoin.crypto.ecc.Secp256k1Point;
import bitcoin.crypto.ecc.Signature;
import bitcoin.lang.dtype.Data;
import bitcoin.lang.dtype.HexString;
import bitcoin.lang.dtype.IntValue;
import bitcoin.lang.dtype.NormalString;

public final class OpCheckMultiSig extends OpCode {
	public static final OpCheckMultiSig INSTANCE = new OpCheckMultiSig();
	
	private OpCheckMultiSig() {
		register(174,  this);
	}
	
	public String getName() {
		return "OP_CHECKMULTISIG";
	}
	
	protected boolean exec(Context context) {
		Stack<Data<?>> inputs = context.getRegularStack();
		if (inputs.isEmpty()) {
			return false;
		}
		
		int n = parseNumber(inputs.pop());
		if (n < 1 || inputs.size() < n) {
			return false;
		}
		
		Secp256k1Point[] pubKeys = new Secp256k1Point[n];
		for (int i = 0; i < n; ++i) {
			try {
				pubKeys[i] = parsePublicKey(inputs.pop());
			} catch(Exception e) {
				return false;
			}
		}
		
		int m = parseNumber(inputs.pop());
		if (m < 1 || inputs.size() < m || m > n) {
			return false;
		}
		
		Signature[] signatures = new Signature[m];
		for (int i = 0; i < m; ++i) {
			try {
				signatures[i] = parseSignature(inputs.pop());
			} catch(Exception e) {
				return false;
			}
		}
		
		if (inputs.size() < 2) {
			return false;
		}
		
		inputs.pop(); // This is to handle the off-by-one error ...
		
		BigInteger z;
		try {
			z = parseZ(inputs.pop());
		} catch(Exception e) {
			return false;
		}	
		
		for (int i = 0; i < m; ++i) {
			if (!pubKeys[i].verifySignature(z, signatures[i])) {
				inputs.push(NormalString.FAILURE);
				return false;
			}
		}
		
		inputs.push(IntValue.SUCCESS);		
		return true;
	}
	
	public byte toByte() {
		return (byte)174; 
	}
	
	private int parseNumber(Data<?> data) {
		if (data instanceof IntValue) {
			IntValue i = (IntValue)data;
			return i.read().intValue();
		}
		
		return bytesToInt(data.readAsBytes());
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
			return new BigInteger(hex.read(), 16);
		}
		
		String hex = bytesToHex(data.readAsBytes());
		return new BigInteger(hex, 16);
	}
}
