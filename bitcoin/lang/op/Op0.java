package bitcoin.lang.op;

import java.util.Stack;

import bitcoin.lang.dtype.Data;
import bitcoin.lang.dtype.NormalString;

public final class Op0 extends OpCode {
	public static final Op0 INSTANCE = new Op0();
	
	private Op0() {
		register(0,  this);
	}
	
	public String getName() {
		return "OP_0";
	}
	
	protected boolean exec(Context context) {
		Stack<Data<?>> inputs = context.getRegularStack();		
		
		inputs.push(NormalString.FAILURE);
		
		return true;
	}
	
	public byte toByte() {
		return 0; 
	}
}
