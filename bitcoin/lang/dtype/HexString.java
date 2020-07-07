package bitcoin.lang.dtype;

import static bitcoin.util.Bytes.hexToBytes;

public final class HexString extends Data<String> {
	
	public HexString(String data) {
		super(data);
	}
	
	public final Data<String> replicate() {
		return new HexString(super.content);
	}
	
	public String toString() {
		return super.content;
	}

	protected String safeCopy() {
		return super.content;
	}	
	
	protected byte[] asBytes(String data) {
		return hexToBytes(data);
	}
}
