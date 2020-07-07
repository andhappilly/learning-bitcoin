package bitcoin.lang.dtype;

import static bitcoin.util.BigInt.from;
import static bitcoin.util.BigInt.toLittleEndian;
import static bitcoin.util.Functions.checkNull;

import java.util.Arrays;

import bitcoin.lang.Command;
import bitcoin.util.OutputSink;

public abstract class Data<T> extends Command {
	protected T content;
	
	protected Data(T content) {
		checkNull(content);
		
		this.content = content;
	}
	
	public final void writeTo(OutputSink sink) {
		checkNull(sink);
		
		byte[] content = readAsBytes();
		byte[] length;
		if (content.length < 76) {
			length = toLittleEndian(from(content.length), 1);
		} else if (content.length > 75 && content.length < 256) {
			length = toLittleEndian(from(content.length), 1);
			
			byte[] opCode = toLittleEndian(from(76), 1);					
			sink.write(opCode);
		} else if (content.length > 255 && content.length <= 520) {
			length = toLittleEndian(from(content.length), 2);
			
			byte[] opCode = toLittleEndian(from(77), 1);
			sink.write(opCode);					
		} else {
			throw new IllegalArgumentException("Data is too long.");
		}
		
		sink.write(length);
		sink.write(content);
	}
	
	public final boolean equals(Object another) {
		if (another instanceof Data) {
			Data<?> other = (Data<?>)another;
			return Arrays.equals(this.readAsBytes(), other.readAsBytes());
		}
		
		return false;
	}
	
	public final boolean isExecutable() {
		return false;
	}
	
	public final T read() {				
		return safeCopy();
	}
	
	public final byte[] readAsBytes() {
		T content = read();
		return asBytes(content);
	}
	
	public abstract Data<T> replicate();
	
	protected abstract byte[] asBytes(T content);
	protected abstract T safeCopy();
}
