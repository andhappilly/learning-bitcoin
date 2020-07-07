package bitcoin.lang.op;

import java.util.Stack;

import bitcoin.lang.dtype.Data;
import bitcoin.lang.dtype.IntValue;

public final class OpMul extends OpCode {
	public static final OpMul INSTANCE = new OpMul();
	
	private OpMul() {
		register(149,  this);
	}
	
	public String getName() {
		return "OP_MUL";
	}
	
	protected boolean exec(Context context) {
		Stack<Data<?>> inputs = context.getRegularStack();
		if (inputs.size() < 2) {
			return false;
		}
		
		IntValue av, bv;
		
		Data<?> a = inputs.pop();
		Data<?> b = inputs.pop();
		
		if (a instanceof IntValue) {
			av = (IntValue)a;
			bv = (IntValue)b;
		} else {
			try {
				av = IntValue.fromBytes(a.readAsBytes());
				bv = IntValue.fromBytes(b.readAsBytes());
			} catch(Exception e) {
				return false;
			}
		}
		
		inputs.push(new IntValue(av.read() * bv.read()));
		return true;
	}
	
	public byte toByte() {
		return (byte)149; 
	}
}
