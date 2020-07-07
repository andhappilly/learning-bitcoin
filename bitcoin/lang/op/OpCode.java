package bitcoin.lang.op;

import static bitcoin.util.Functions.areEqual;
import static bitcoin.util.Functions.checkNull;

import java.util.HashMap;
import java.util.Map;

import bitcoin.lang.Executable;
import bitcoin.util.OutputSink;

public abstract class OpCode extends Executable {		
	private static final Map<Integer, OpCode> REGISTRY = new HashMap<Integer, OpCode>();
	
	public abstract String getName();
	
	public abstract byte toByte();
	
	public final String toString() {
		return getName();
	}
	
	public final void writeTo(OutputSink sink) {
		checkNull(sink);
		
		sink.write(toByte());
	}
	
	public final boolean equals(Object other) {
		if (other instanceof OpCode) {
			if (areEqual(this.getClass(), other.getClass())) {
				OpCode another = (OpCode)other;
				return this.toByte() == another.toByte();
			}
		}
		
		return false;
	}
	
	public static final OpCode from(int value) {
		return REGISTRY.get(Integer.valueOf(value));
	}
	
	protected final void register(int value, OpCode thiz) {
		REGISTRY.put(Integer.valueOf(value), thiz);
	}
	
	protected final boolean beforeExec(Context context) {
		clearAltStack(context);
		return true;
	}
	
	protected final void afterExec(Context context) {
		clearAltStack(context);
	}	 
}
