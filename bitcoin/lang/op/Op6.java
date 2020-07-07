package bitcoin.lang.op;

import java.util.Stack;

import bitcoin.lang.dtype.Data;
import bitcoin.lang.dtype.IntValue;

public final class Op6 extends OpCode {
	public static final Op6 INSTANCE = new Op6();
	
	private static final IntValue SIX = new IntValue(6);
	
	private Op6() {
		register(86,  this);
	}
	
	public String getName() {
		return "OP_6";
	}
	
	protected boolean exec(Context context) {
		Stack<Data<?>> inputs = context.getRegularStack();		
		
		inputs.push(SIX);
		
		return true;
	}
	
	public byte toByte() {
		return 86; 
	}
}
