package bitcoin.lang.dtype;

import static bitcoin.util.Bytes.bytesToInt;
import static bitcoin.util.Bytes.intToBytes;
import static bitcoin.util.Functions.checkNull;

public final class IntValue extends Data<Integer> {
	public static final IntValue ZERO = new IntValue(Integer.valueOf(0));
	public static final IntValue SUCCESS = new IntValue(Integer.valueOf(1));
	
	public IntValue(Integer data) {
		super(data);
	}
	
	public final Data<Integer> replicate() {
		return new IntValue(super.content);
	}
	
	public String toString() {
		return super.content.toString();
	}
	
	public static IntValue fromBytes(byte[] b) {
		checkNull(b);
		
		if (b.length == 0 || b.length > 4) {
			throw new IllegalArgumentException("byte array not within integer ranage.");
		}
		
		return new IntValue(bytesToInt(b));
	}
	
	protected Integer safeCopy() {
		return super.content;
	}	
	
	protected byte[] asBytes(Integer data) {
		return intToBytes(data.intValue());
	}
}
