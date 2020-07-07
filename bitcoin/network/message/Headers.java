package bitcoin.network.message;

import static bitcoin.util.BigInt.from;
import static bitcoin.util.BigInt.fromVarInt;
import static bitcoin.util.BigInt.getVarIntSize;
import static bitcoin.util.BigInt.toVarInt;
import static bitcoin.util.Bytes.hexCharsToByte;
import static bitcoin.util.Bytes.hexToBytes;
import static bitcoin.util.Functions.checkNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import bitcoin.core.Block;
import bitcoin.network.Message;
import bitcoin.util.Bytes;
import bitcoin.util.InputSource;
import bitcoin.util.OutputSink;

public final class Headers extends Message {
	public static final String TYPE = "headers";
	
	static {
		register(Headers.class, TYPE);
	}
	
	private List<Block> blocks;
	
	public Headers(List<Block> blocks) {
		super(TYPE);
		
		checkNull(blocks);
		
		this.blocks = blocks;
	}
	
	public void writeTo(OutputSink sink) {
		checkNull(sink);
		
		// First write the block count ...
		byte[] bytes = toVarInt(from(blocks.size()));
		sink.write(bytes);
		
		// Next write all the blocks ...
		for (Block b: blocks) {
			b.writeTo(sink);
		}
		
		// Finally write the transaction count as zero ...
		sink.write(Bytes.ZERO);
	}
	
	public static Headers parse(InputSource source) {
		checkNull(source);
		
		// The "number of headers" field is of variable length, 
		// hence need to read the next byte to determine the size ..
		char[] chars = source.readNextChars(2);
		byte b = hexCharsToByte(chars[0], chars[1]);
		int vIntSize = getVarIntSize(b);		
		
		BigInteger headersCount;
		if (vIntSize == 1) {
			// If the variable integer size is 1, the already read byte represents the number ...
			headersCount = BigInteger.valueOf(b);
		} else {
			// Read the next 2*vIntSize characters that represent the number ...
			chars = source.readNextChars(2*vIntSize);
			byte[] bytes = hexToBytes(chars);
			headersCount = fromVarInt(bytes);
		}	
		
		int numberOfHeaders = headersCount.intValue();
		List<Block> blocks = new ArrayList<Block>(numberOfHeaders);
		for (int i = 0; i < numberOfHeaders; ++i) {
			Block block = Block.parse(source);
			blocks.add(block);
		}
		
		// The "number of transactions" field is of variable length, 
		// hence need to read the next byte to determine the size ..
		chars = source.readNextChars(2);
		b = hexCharsToByte(chars[0], chars[1]);
		vIntSize = getVarIntSize(b);		
		
		BigInteger txCount;
		if (vIntSize == 1) {
			// If the variable integer size is 1, the already read byte represents the number ...
			txCount = BigInteger.valueOf(b);
		} else {
			// Read the next 2*vIntSize characters that represent the number ...
			chars = source.readNextChars(2*vIntSize);
			byte[] bytes = hexToBytes(chars);
			txCount = fromVarInt(bytes);
		}
		
		// In a valid "headers" message transaction count should be always zero ...
		if (!BigInteger.ZERO.equals(txCount)) {
			throw new IllegalArgumentException("Invalid headers.");
		}
		
		return new Headers(blocks);
	}
}
