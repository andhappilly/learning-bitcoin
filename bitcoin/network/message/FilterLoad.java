package bitcoin.network.message;

import static bitcoin.util.BigInt.from;
import static bitcoin.util.BigInt.fromLittleEndian;
import static bitcoin.util.BigInt.fromVarInt;
import static bitcoin.util.BigInt.getVarIntSize;
import static bitcoin.util.BigInt.toLittleEndian;
import static bitcoin.util.BigInt.toVarInt;
import static bitcoin.util.Bytes.ONE;
import static bitcoin.util.Bytes.hexCharsToByte;
import static bitcoin.util.Bytes.hexToBytes;
import static bitcoin.util.Functions.checkNull;

import java.math.BigInteger;

import bitcoin.network.Message;
import bitcoin.util.BloomFilter;
import bitcoin.util.InputSource;
import bitcoin.util.OutputSink;

public final class FilterLoad extends Message {
	public static final String TYPE = "filterload";
	
	static {
		register(FilterLoad.class, TYPE);
	}
	
	private BloomFilter filter;
	private byte flag;
	
	public FilterLoad(BloomFilter filter) {
		this(filter, ONE);
	}
	
	public FilterLoad(BloomFilter filter, byte flag) {
		super(TYPE);
		
		checkNull(filter, flag);
		
		this.filter = filter;
		this.flag = flag;
	}

	public void writeTo(OutputSink sink) {
		checkNull(sink);
		
		byte[] bitsField = filter.getBitsField();
		
		// Write the bitsField size first ...
		byte[] bytes = toVarInt(from(bitsField.length));
		sink.write(bytes);
		
		// Write the bits field next ...
		sink.write(bitsField);
		
		BigInteger functionCount = filter.getFunctionCount();
		
		// Write the function count next ...
		bytes = toLittleEndian(functionCount, 4);
		sink.write(bytes);
		
		BigInteger tweak = filter.getTweak();
		
		// Write the tweak field next ...
		bytes = toLittleEndian(tweak, 4);
		sink.write(bytes);	
		
		// Write the flag field next ...		
		sink.write(bytes);			
	}
	
	public static FilterLoad parse(InputSource source) {
		checkNull(source);
		
		// The "bits" field is of variable length, 
		// hence need to read the next byte to determine the size ..
		char[] chars = source.readNextChars(2);
		byte b = hexCharsToByte(chars[0], chars[1]);
		int vIntSize = getVarIntSize(b);		
		
		BigInteger size;
		if (vIntSize == 1) {
			// If the variable integer size is 1, the already read byte represents the size ...
			size = BigInteger.valueOf(b);
		} else {
			// Read the next 2*vIntSize characters that represent the size ...
			chars = source.readNextChars(2*vIntSize);
			byte[] bytes = hexToBytes(chars);
			size = fromVarInt(bytes);
		}
		
		chars = source.readNextChars(2*size.intValue());
		byte[] bitsField = hexToBytes(chars);
		
		// Next read the function count (4 bytes or 8 characters) ...
		chars = source.readNextChars(8);
		byte[] bytes = hexToBytes(chars);
		BigInteger functionCount = fromLittleEndian(bytes);
		
		// Next read the tweak (4 bytes or 8 characters) ...
		chars = source.readNextChars(8);
		bytes = hexToBytes(chars);
		BigInteger tweak = fromLittleEndian(bytes);	
		
		// Next read the flag field (1 byte or 2 characters) ...
		chars = source.readNextChars(2);
		byte flag = hexCharsToByte(chars[0], chars[1]);	
		
		BloomFilter filter = BloomFilter.from(bitsField, functionCount, tweak);
		
		return new FilterLoad(filter, flag);
	}
}
