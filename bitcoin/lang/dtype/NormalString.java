package bitcoin.lang.dtype;

public final class NormalString extends Data<String> {
	public static final NormalString FAILURE = new NormalString("");
	
	public NormalString(String data) {
		super(data);
	}
	
	public final Data<String> replicate() {
		return new NormalString(super.content);
	}
	
	public String toString() {
		return super.content;
	}

	protected String safeCopy() {
		return super.content;
	}	
	
	protected byte[] asBytes(String data) {
		return data.getBytes();
	}
}
