package bitcoin.lang.op;

import java.util.Stack;

import bitcoin.lang.dtype.Data;

public final class OpSwap extends OpCode {
public static final OpSwap INSTANCE = new OpSwap();
	
	private OpSwap() {
		register(124,  this);
	}
	
	public String getName() {
		return "OP_SWAP";
	}
	
	protected boolean exec(Context context) {
		Stack<Data<?>> inputs = context.getRegularStack();
		if (inputs.size() < 2) {
			return false;
		}
		
		Data<?> top1 = inputs.pop();		
		Data<?> top2 = inputs.pop();
		
		inputs.push(top1);
		inputs.push(top2);
		
		return true;
	}
	
	public byte toByte() {
		return 124; 
	}
}
