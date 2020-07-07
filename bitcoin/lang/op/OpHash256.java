package bitcoin.lang.op;

import static bitcoin.util.Crypto.hash256;

import java.util.Stack;

import bitcoin.lang.dtype.ByteArray;
import bitcoin.lang.dtype.Data;

public final class OpHash256 extends OpCode {

	public static final OpHash256 INSTANCE = new OpHash256();
	
	private OpHash256() {
		register(170,  this);
	}
	
	public String getName() {
		return "OP_HASH256";
	}
	
	protected boolean exec(Context context) {
		Stack<Data<?>> inputs = context.getRegularStack();
		if (inputs.isEmpty()) {
			return false;
		}
		
		Data<?> top = inputs.pop();	
		
		try {
			byte[] content = top.readAsBytes();
			inputs.push(new ByteArray(hash256(content)));
		} catch(Exception e) {
			return false;
		}
		
		return true;
	}
	
	public byte toByte() {
		return (byte)170; 
	}
}
