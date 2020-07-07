package bitcoin.lang.op;

import static bitcoin.util.Functions.areEqual;

import java.util.Arrays;
import java.util.Stack;

import bitcoin.lang.dtype.Data;
import bitcoin.lang.dtype.IntValue;
import bitcoin.lang.dtype.NormalString;

public final class OpEqual extends OpCode {
	public static final OpEqual INSTANCE = new OpEqual();
	
	private OpEqual() {
		register(135,  this);
	}
	
	public String getName() {
		return "OP_EQUAL";
	}
	
	protected boolean exec(Context context) {
		Stack<Data<?>> inputs = context.getRegularStack();
		if (inputs.size() < 2) {
			return false;
		}
		
		boolean eq;
		
		Data<?> a = inputs.pop();
		Data<?> b = inputs.pop();
		
		if (a.getClass() == b.getClass()) {
			eq = areEqual(a, b);
		} else {
			byte[] one = a.readAsBytes();
			byte[] two = b.readAsBytes();
			
			eq = Arrays.equals(one, two);
		}
		
		inputs.push(eq ? IntValue.SUCCESS : NormalString.FAILURE);
		
		return true;
	}
	
	public byte toByte() {
		return (byte)135; 
	}
}
