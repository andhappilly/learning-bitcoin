package bitcoin.lang.op;

import java.util.Stack;

import bitcoin.lang.dtype.Data;

public final class Op2Dup extends OpCode {
	public static final Op2Dup INSTANCE = new Op2Dup();
	
	private Op2Dup() {
		register(110,  this);
	}
	
	public String getName() {
		return "OP_2DUP";
	}
	
	protected boolean exec(Context context) {
		Stack<Data<?>> inputs = context.getRegularStack();
		if (inputs.size() < 2) {
			return false;
		}
		
		Data<?> top1 = inputs.pop();
		Data<?> top11 = top1.replicate();
		Data<?> top2 = inputs.pop();
		Data<?> top21 = top2.replicate();
		
		inputs.push(top2);
		inputs.push(top1);
		inputs.push(top21);
		inputs.push(top11);
		
		return true;
	}
	
	public byte toByte() {
		return 110; 
	}
}
