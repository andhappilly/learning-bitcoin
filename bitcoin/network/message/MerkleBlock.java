package bitcoin.network.message;

import static bitcoin.util.BigInt.from;
import static bitcoin.util.BigInt.fromLittleEndian;
import static bitcoin.util.BigInt.fromVarInt;
import static bitcoin.util.BigInt.getVarIntSize;
import static bitcoin.util.BigInt.toLittleEndian;
import static bitcoin.util.BigInt.toVarInt;
import static bitcoin.util.Bytes.ensureSize;
import static bitcoin.util.Bytes.hexCharsToByte;
import static bitcoin.util.Bytes.hexToBytes;
import static bitcoin.util.Functions.checkNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import bitcoin.network.Message;
import bitcoin.util.InputSource;
import bitcoin.util.MerkleTree;
import bitcoin.util.OutputSink;

public final class MerkleBlock extends Message {
	public static final String TYPE = "merkleblock";
	
	static {
		register(MerkleBlock.class, TYPE);
	}
	
	private BigInteger version;	
	private BigInteger pvBlkHash;
	private BigInteger mrklRootHash;
	private BigInteger timestamp;
	private BigInteger bits;
	private BigInteger nonce;
	private BigInteger txNum;
	private List<BigInteger> txHashes;
	private byte[] flags;
	
	public MerkleBlock(BigInteger version, BigInteger pvBlkHash, BigInteger mrklRootHash, BigInteger timestamp,
			BigInteger bits, BigInteger nonce, BigInteger txNum, List<BigInteger> txHashes, byte[] flags) {
		super(TYPE);
		
		checkNull(version, pvBlkHash, mrklRootHash, timestamp, bits, nonce, txHashes, flags);
		
		this.version = version;
		this.pvBlkHash = pvBlkHash;
		this.mrklRootHash = mrklRootHash;
		this.timestamp = timestamp;
		this.bits = bits;
		this.nonce = nonce;
		this.txNum = txNum;
		this.txHashes = Collections.unmodifiableList(txHashes);
		this.flags = flags;
	}
	
	public boolean isValid() {	
		List<BigInteger> reversedHashes = MerkleTree.reversesOf(txHashes);
		List<Boolean> bitFlags = bytesToBitFlags();
		
		MerkleTree tree = new MerkleTree(txNum.intValue());		
		tree.populate(bitFlags, reversedHashes);
		
		return mrklRootHash.equals(MerkleTree.reverseOf(tree.getRoot()));
	}

	public void writeTo(OutputSink sink) {
		checkNull(sink);
		
		// First write the version ...
		byte[] bytes = toLittleEndian(version, 4);
		sink.write(bytes);
		
		// Next write the previous block hash ...
		bytes = toLittleEndian(pvBlkHash, 32);
		sink.write(bytes);
		
		// Next write the merle root hash ...
		bytes = toLittleEndian(mrklRootHash, 32);
		sink.write(bytes);		
		
		// Next write the timestamp ...
		bytes = toLittleEndian(timestamp, 4);
		sink.write(bytes);
		
		// Next write the bits
		bytes = ensureSize(bits.toByteArray(), 4);		
		sink.write(bytes);
		
		// Next write the nonce
		bytes = ensureSize(nonce.toByteArray(), 4);	
		sink.write(bytes);	
		
		// Next write the total number of transactions ...
		bytes = toLittleEndian(txNum, 4);
		sink.write(bytes);		
		
		// Next write the transaction hash count ...
		bytes = toVarInt(from(txHashes.size()));
		sink.write(bytes);
		
		// Next write the block ids ...
		for (BigInteger hash: txHashes) {
			bytes = toLittleEndian(hash, 32);
			sink.write(bytes);
		}	
		
		// Next write the flags count ...
		bytes = toVarInt(from(flags.length));
		sink.write(bytes);		
		
		// Finally write the flag bytes ...
		sink.write(flags);
	}
	
	public static MerkleBlock parse(InputSource source) {
		checkNull(source);
		
		// Read the version bytes (4 bytes or 8 characters) first ...
		char[] chars = source.readNextChars(8);
		byte[] bytes = hexToBytes(chars);
		
		BigInteger version = fromLittleEndian(bytes);
		
		// Next read the previous block hash (32 bytes or 64 characters) ...
		chars = source.readNextChars(64);
		bytes = hexToBytes(chars);
		
		BigInteger pvBlkHash = fromLittleEndian(bytes);
		
		// Next read the Merkle root hash (32 bytes or 64 characters) ...
		chars = source.readNextChars(64);
		bytes = hexToBytes(chars);
		
		BigInteger mrklRootHash = fromLittleEndian(bytes);
		
		// Next read the timestamp (4 bytes or 8 characters) ...
		chars = source.readNextChars(8);
		bytes = hexToBytes(chars);
		
		BigInteger timestamp = fromLittleEndian(bytes);
		
		// Next read the bits (4 bytes or 8 characters) ...
		chars = source.readNextChars(8);
		bytes = hexToBytes(chars);
				
		BigInteger bits = new BigInteger(1, bytes);
		
		// Next read the nonce (4 bytes or 8 characters) ...
		chars = source.readNextChars(8);
		bytes = hexToBytes(chars);
				
		BigInteger nonce = new BigInteger(1, bytes);
		
		// Next read the transaction count (4 bytes or 8 characters) ...
		chars = source.readNextChars(8);
		bytes = hexToBytes(chars);
		
		BigInteger txNum = fromLittleEndian(bytes);
		
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
		List<BigInteger> txHashes = new ArrayList<BigInteger>(numberOfHashes);
		for (int i = 0; i < numberOfHashes; ++i) {
			// Get the id of the starting block ...
			chars = source.readNextChars(64);	
			bytes = hexToBytes(chars);
			BigInteger hash = fromLittleEndian(bytes);
			
			txHashes.add(hash);
		}
		
		// The "flag bits" field is of variable length, 
		// hence need to read the next byte to determine the size ..
		chars = source.readNextChars(2);
		b = hexCharsToByte(chars[0], chars[1]);
		vIntSize = getVarIntSize(b);		
		
		BigInteger flagsCount;
		if (vIntSize == 1) {
			// If the variable integer size is 1, the already read byte represents the number ...
			flagsCount = BigInteger.valueOf(b);
		} else {
			// Read the next 2*vIntSize characters that represent the number ...
			chars = source.readNextChars(2*vIntSize);
			bytes = hexToBytes(chars);
			flagsCount = fromVarInt(bytes);
		}
		
		chars = source.readNextChars(2*flagsCount.intValue());
		byte[] flags = hexToBytes(chars);
		
		return new MerkleBlock(version, pvBlkHash, mrklRootHash, timestamp, bits, nonce, txNum, txHashes, flags);
	}
	
	private List<Boolean> bytesToBitFlags() {
		List<Boolean> bitFlags = new ArrayList<Boolean>();
		for (byte b: flags) {
			int f = (int)b;
			for (int i = 0; i < 8; ++i) {
				bitFlags.add((f & 1) == 1);
				f = f >> 1;
			}
		}
		
		return bitFlags;
	}
}
