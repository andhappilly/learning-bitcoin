package bitcoin.network.message;

import static bitcoin.util.BigInt.from;
import static bitcoin.util.BigInt.fromLittleEndian;
import static bitcoin.util.BigInt.fromVarInt;
import static bitcoin.util.BigInt.getVarIntSize;
import static bitcoin.util.BigInt.toLittleEndian;
import static bitcoin.util.BigInt.toVarInt;
import static bitcoin.util.Bytes.hexCharsToByte;
import static bitcoin.util.Bytes.hexToBytes;
import static bitcoin.util.Bytes.newBytes;
import static bitcoin.util.Functions.checkNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import bitcoin.network.Message;
import bitcoin.util.InputSource;
import bitcoin.util.OutputSink;

public final class GetHeaders extends Message {
	public static final String TYPE = "getheaders";
	
	private static final BigInteger DEFAULT_VERSION = from(70015);
	private static final BigInteger DEFAULT_END_BLOCK_ID = new BigInteger(1, newBytes(32, (byte)0));
	
	static {
		register(GetHeaders.class, TYPE);
	}
	
	private BigInteger version;
	private List<BigInteger> blockIds;
	private BigInteger endingBlockId;
	
	public GetHeaders(List<BigInteger> blockIds) {
		this(DEFAULT_VERSION, blockIds, DEFAULT_END_BLOCK_ID);
	}

	public GetHeaders(BigInteger version, List<BigInteger> blockIds, BigInteger endingBlockId) {
		super(TYPE);
		
		checkNull(version, blockIds);
		
		this.version = version;
		this.blockIds = Collections.unmodifiableList(blockIds);
		this.endingBlockId = endingBlockId == null ? DEFAULT_END_BLOCK_ID : endingBlockId;
	}

	public void writeTo(OutputSink sink) {
		checkNull(sink);
		
		// First write the version bytes ...
		byte[] bytes = toLittleEndian(version, 4);
		sink.write(bytes);
		
		// Next write the hash count ...
		bytes = toVarInt(from(blockIds.size()));
		sink.write(bytes);
		
		// Next write the block ids ...
		for (BigInteger id: blockIds) {
			bytes = toLittleEndian(id, 32);
			sink.write(bytes);
		}
		
		// Next write the ending block id ...
		bytes = toLittleEndian(endingBlockId, 32);
		sink.write(bytes);		
	}
	
	public static GetHeaders parse(InputSource source) {
		checkNull(source);
		
		// First read the version 
		char[] chars = source.readNextChars(8);
		byte[] bytes = hexToBytes(chars);
		BigInteger version = fromLittleEndian(bytes);
		
		// The "number of hashes" field is of variable length, 
		// hence need to read the next byte to determine the size ..
		chars = source.readNextChars(2);
		byte b = hexCharsToByte(chars[0], chars[1]);
		int vIntSize = getVarIntSize(b);		
		
		BigInteger hashCount;
		if (vIntSize == 1) {
			// If the variable integer size is 1, the already read byte represents the number ...
			hashCount = BigInteger.valueOf(b);
		} else {
			// Read the next 2*vIntSize characters that represent the number ...
			chars = source.readNextChars(2*vIntSize);
			bytes = hexToBytes(chars);
			hashCount = fromVarInt(bytes);
		}
		
		int numberOfHashes = hashCount.intValue();
		List<BigInteger> blockIds = new ArrayList<BigInteger>(numberOfHashes);
		for (int i = 0; i < numberOfHashes; ++i) {
			// Get the id of the starting block ...
			chars = source.readNextChars(64);	
			bytes = hexToBytes(chars);
			BigInteger id = fromLittleEndian(bytes);
			
			blockIds.add(id);
		}
		
		// Get the id of the ending block ...
		chars = source.readNextChars(64);	
		bytes = hexToBytes(chars);
		BigInteger endBlockId = fromLittleEndian(bytes);
		
		return new GetHeaders(version, blockIds, endBlockId);
	}
}
