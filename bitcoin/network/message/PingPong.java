package bitcoin.network.message;

import static bitcoin.util.Functions.checkNull;

import bitcoin.network.Message;
import bitcoin.util.OutputSink;

abstract class PingPong extends Message {
	private byte[] nonce;
	
	public PingPong(byte[] nonce, String type) {
		super(type);
		
		checkNull(nonce);
		
		this.nonce = nonce;
	}
	
	public void writeTo(OutputSink sink) {
		sink.write(nonce);
	}
}
