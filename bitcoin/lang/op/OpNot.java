package bitcoin.lang.op;

import static bitcoin.util.Functions.areEqual;

import java.util.Stack;

import bitcoin.lang.dtype.Data;
import bitcoin.lang.dtype.IntValue;
import bitcoin.lang.dtype.NormalString;

public final class OpNot extends OpCode {
	public static final OpNot INSTANCE = new OpNot();
	
	private OpNot() {
		register(145,  this);
	}
	
	public String getName() {
		return "OP_NOT";
	}
	
	protected boolean exec(Context context) {
		Stack<Data<?>> inputs = context.getRegularStack();
		if (inputs.isEmpty()) {
			return false;
		}
		
		Data<?> top = inputs.pop();
		
		if (top instanceof IntValue) {					
			boolean zero = areEqual(IntValue.ZERO, top);				
			inputs.push(zero ? IntValue.SUCCESS : IntValue.ZERO);
			return true;
		}
		
		if (top instanceof NormalString) {			
			boolean zero = areEqual(NormalString.FAILURE, top);		
			if (zero) {
				inputs.push(IntValue.SUCCESS);
				return true;
			}			
		}		
		
		try {
			boolean zero = areEqual(IntValue.ZERO, 
					IntValue.fromBytes(top.readAsBytes()));
			inputs.push(zero ? IntValue.SUCCESS : IntValue.ZERO);
			return true;
		} catch(Exception e) {
			return false;
		}
	}
	
	public byte toByte() {
		return (byte)145; 
	}
}
