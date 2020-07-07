package bitcoin.lang.dtype;

import static bitcoin.util.Bytes.bytesToHex;
import static bitcoin.util.Bytes.newBytes;

public final class ByteArray extends Data<byte[]> {
	
	public ByteArray(byte[] data) {
		super(data);
	}
	
	public final Data<byte[]> replicate() {
		return new ByteArray(super.content);
	}
	
	public String toString() {
		return bytesToHex(super.content);
	}

	protected byte[] safeCopy() {	
		return newBytes(super.content);
	}	
	
	protected byte[] asBytes(byte[] data) {
		return safeCopy();
	}
}
