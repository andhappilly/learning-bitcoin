package bitcoin.util;

import static bitcoin.util.Bytes.EMPTY_BYTES;
import static bitcoin.util.Bytes.concatenate;

public final class OutputSink {
	
	private static final class Bytes {
		byte[] data;
		Bytes next;
		
		Bytes(byte[] data) {
			this.data = data;
		}
	}
	
	private Bytes root;
	private Bytes current;
	private int length;
	
	public OutputSink() {
		this.root = new Bytes(EMPTY_BYTES);
		this.current = root;
		++length;
	}
	
	public void write(byte data) {
		write(new byte[] {data});
	}
	
	public void write(byte[] data) {
		Bytes b = new Bytes(data);
		current.next = b;
		current = b;
		++length;
	}
	
	public byte[] toByteArray() {
		byte[][] bytesArray = new byte[length][];
		Bytes b = root;
		for (int i = 0; i < length; ++i) {
			bytesArray[i] = b.data;
			b = b.next;
		}
		
		return concatenate(bytesArray);
	}
}
