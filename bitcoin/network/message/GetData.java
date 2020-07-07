package bitcoin.network.message;

import static bitcoin.util.BigInt.from;
import static bitcoin.util.BigInt.fromLittleEndian;
import static bitcoin.util.BigInt.fromVarInt;
import static bitcoin.util.BigInt.getVarIntSize;
import static bitcoin.util.BigInt.toLittleEndian;
import static bitcoin.util.BigInt.toVarInt;
import static bitcoin.util.Bytes.hexCharsToByte;
import static bitcoin.util.Bytes.hexToBytes;
import static bitcoin.util.Functions.checkNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import bitcoin.network.Message;
import bitcoin.util.InputSource;
import bitcoin.util.OutputSink;

public final class GetData extends Message {
	public static final String TYPE = "getdata";
	
	public static final class Data {
		private BigInteger hType;
		private BigInteger hash; 
		
		private Data(BigInteger hType, BigInteger hash) {
			this.hType = hType;
			this.hash = hash;
		}
	}
	
	static {
		register(GetData.class, TYPE);
	}
	
	private List<Data> hashes;
	
	public GetData(List<Data> hashes) {
		super(TYPE);
		
		checkNull(hashes);
		
		this.hashes = Collections.unmodifiableList(hashes);
	}

	public void writeTo(OutputSink sink) {
		// First write the hash count ...
		byte[] bytes = toVarInt(from(hashes.size()));
		sink.write(bytes);
		
		// Now write all the hashes ...
		for (Data d: hashes) {
			bytes = toLittleEndian(d.hType, 4);
			sink.write(bytes);		
			
			bytes = toLittleEndian(d.hash, 32);
			sink.write(bytes);
		}			
	}
	
	public static GetData parse(InputSource source) {
		// The "number of hashes" field is of variable length, 
		// hence need to read the next byte to determine the size ..
		char[] chars = source.readNextChars(2);
		byte b = hexCharsToByte(chars[0], chars[1]);
		int vIntSize = getVarIntSize(b);		
		
		BigInteger hashCount;
		byte[] bytes = null;
		if (vIntSize == 1) {
			// If the variable integer size is 1, the already read byte represents the number ...
			hashCount = BigInteger.valueOf(b);
		} else {
			// Read the next 2*vIntSize characters that represent the number ...
			chars = source.readNextChars(2*vIntSize);
			bytes = hexToBytes(chars);
			hashCount = fromVarInt(bytes);
		}
		
		// Now read the hashes ...
		int numberOfHashes = hashCount.intValue();
		List<Data> hashes = new ArrayList<Data>(numberOfHashes);
		for (int i = 0; i < numberOfHashes; ++i) {
			chars = source.readNextChars(8);
			bytes = hexToBytes(chars);
			
			BigInteger hType = fromLittleEndian(bytes);	
			
			// Get the id of the starting block ...
			chars = source.readNextChars(64);	
			bytes = hexToBytes(chars);
			BigInteger hash = fromLittleEndian(bytes);
			
			hashes.add(new Data(hType, hash));
		}
		
		return new GetData(hashes);
	}
}
