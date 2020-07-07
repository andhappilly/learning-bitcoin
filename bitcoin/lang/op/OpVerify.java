package bitcoin.lang.op;

import static bitcoin.util.Functions.areEqual;

import java.util.Stack;

import bitcoin.lang.dtype.Data;
import bitcoin.lang.dtype.IntValue;
import bitcoin.lang.dtype.NormalString;

public final class OpVerify extends OpCode {
	public static final OpVerify INSTANCE = new OpVerify();
	
	private OpVerify() {
		register(105,  this);
	}
	
	public String getName() {
		return "OP_VERIFY";
	}
	
	protected boolean exec(Context context) {
		Stack<Data<?>> inputs = context.getRegularStack();
		if (inputs.isEmpty()) {
			return false;
		}
		
		Data<?> top = inputs.peek();
		
		if (areEqual(NormalString.FAILURE, top)) {
			return false;
		}
		
		if (top instanceof IntValue) {
			if (areEqual(IntValue.ZERO, top)) {
				return false;
			}
			
			inputs.pop();
			return true;
		}		
		
		try {
			if (areEqual(IntValue.ZERO, 
					IntValue.fromBytes(top.readAsBytes()))) {
				return false;
			}
			
			inputs.pop();
			return true;
		} catch(Exception e) {
			return false;
		}
	}
	
	public byte toByte() {
		return 105; 
	}
}
