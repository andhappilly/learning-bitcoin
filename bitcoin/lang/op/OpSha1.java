package bitcoin.lang.op;

import static bitcoin.util.Crypto.sha1;

import java.util.Stack;

import bitcoin.lang.dtype.ByteArray;
import bitcoin.lang.dtype.Data;

public final class OpSha1 extends OpCode {

	public static final OpSha1 INSTANCE = new OpSha1();
	
	private OpSha1() {
		register(167,  this);
	}
	
	public String getName() {
		return "OP_SHA1";
	}
	
	protected boolean exec(Context context) {
		Stack<Data<?> > inputs = context.getRegularStack();
		if (inputs.isEmpty()) {
			return false;
		}
		
		Data<?> top = inputs.pop();	
		
		try {
			byte[] content = top.readAsBytes();
			inputs.push(new ByteArray(sha1(content)));
		} catch(Exception e) {
			return false;
		}
		
		return true;		
	}
	
	public byte toByte() {
		return (byte)167; 
	}
}
