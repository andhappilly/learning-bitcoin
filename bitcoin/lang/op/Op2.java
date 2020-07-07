package bitcoin.lang.op;

import java.util.Stack;

import bitcoin.lang.dtype.Data;
import bitcoin.lang.dtype.IntValue;

public final class Op2 extends OpCode {
	public static final Op2 INSTANCE = new Op2();
	
	private static final IntValue TWO = new IntValue(2);
	
	private Op2() {
		register(82,  this);
	}
	
	public String getName() {
		return "OP_2";
	}
	
	protected boolean exec(Context context) {
		Stack<Data<?>> inputs = context.getRegularStack();		
		
		inputs.push(TWO);
		
		return true;
	}
	
	public byte toByte() {
		return 82; 
	}
}
