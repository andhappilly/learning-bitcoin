package bitcoin.lang.op;

import static bitcoin.util.Crypto.hash160;

import java.util.Stack;

import bitcoin.lang.dtype.ByteArray;
import bitcoin.lang.dtype.Data;

public final class OpHash160 extends OpCode {

	public static final OpHash160 INSTANCE = new OpHash160();
	
	private OpHash160() {
		register(169,  this);
	}
	
	public String getName() {
		return "OP_HASH160";
	}
	
	protected boolean exec(Context context) {
		Stack<Data<?> > inputs = context.getRegularStack();
		if (inputs.isEmpty()) {
			return false;
		}
		
		Data<?> top = inputs.pop();	
		
		try {
			byte[] content = top.readAsBytes();
			inputs.push(new ByteArray(hash160(content)));
		} catch(Exception e) {
			return false;
		}
		
		return true;
	}
	
	public byte toByte() {
		return (byte)169; 
	}
}
