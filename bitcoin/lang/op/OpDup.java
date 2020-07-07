package bitcoin.lang.op;

import java.util.Stack;

import bitcoin.lang.dtype.Data;

public final class OpDup extends OpCode {
	public static final OpDup INSTANCE = new OpDup();
	
	private OpDup() {
		register(118,  this);
	}
	
	public String getName() {
		return "OP_DUP";
	}
	
	protected boolean exec(Context context) {
		Stack<Data<?>> inputs = context.getRegularStack();
		if (inputs.isEmpty()) {
			return false;
		}
		
		inputs.push(inputs.peek().replicate());
		
		return true;
	}
	
	public byte toByte() {
		return 118; 
	}
}
