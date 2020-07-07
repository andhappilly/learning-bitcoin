package bitcoin.lang.op;

public final class OpEqualVerify extends OpCode {
	public static final OpEqualVerify INSTANCE = new OpEqualVerify();
	
	private OpEqualVerify() {
		register(136,  this);
	}
	
	public String getName() {
		return "OP_EQUALVERIFY";
	}
	
	protected boolean exec(Context context) {
		if (OpEqual.INSTANCE.execute(context)) {
			return OpVerify.INSTANCE.execute(context);
		}
		
		return false;
	}
	
	public byte toByte() {
		return (byte)136; 
	}
}
