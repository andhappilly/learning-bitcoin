package bitcoin.util;

public abstract class BytesEncodeable {
	public final byte[] toBytes() {
		OutputSink sink = new OutputSink();
		writeTo(sink);
		return sink.toByteArray();
	}
	
	public abstract void writeTo(OutputSink sink);	
}
