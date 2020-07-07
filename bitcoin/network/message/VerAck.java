package bitcoin.network.message;

import static bitcoin.util.Bytes.EMPTY_BYTES;
import static bitcoin.util.Functions.checkNull;

import bitcoin.network.Message;
import bitcoin.util.InputSource;
import bitcoin.util.OutputSink;

public final class VerAck extends Message {	
	public static final String TYPE = "verack";
	
	public static final VerAck INSTANCE = new VerAck();
	
	static {
		register(VerAck.class, TYPE);
	}
	
	private VerAck() {
		super(TYPE);
	}

	public void writeTo(OutputSink sink) {
		checkNull(sink);
		
		sink.write(EMPTY_BYTES);
	}
	
	public static VerAck parse(InputSource source) {
		checkNull(source);
		
		return INSTANCE; 
	}
}
