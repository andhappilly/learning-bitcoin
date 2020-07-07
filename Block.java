package bitcoin.core;

import static bitcoin.core.PowHelper.bitsToTarget;
import static bitcoin.core.PowHelper.difficultyFromTarget;
import static bitcoin.util.BigInt.fromLittleEndian;
import static bitcoin.util.BigInt.isThisLessThanThat;
import static bitcoin.util.BigInt.toLittleEndian;
import static bitcoin.util.Bytes.bytesToHex;
import static bitcoin.util.Bytes.ensureSize;
import static bitcoin.util.Bytes.hexToBytes;
import static bitcoin.util.Bytes.reverse;
import static bitcoin.util.Crypto.hash256;
import static bitcoin.util.Functions.areEqual;
import static bitcoin.util.Functions.checkNull;
import static bitcoin.util.Functions.isNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import bitcoin.util.BytesEncodeable;
import bitcoin.util.InputSource;
import bitcoin.util.MerkleTree;
import bitcoin.util.OutputSink;

public final class Block extends BytesEncodeable {	
	private BigInteger version;	
	private BigInteger pvBlkHash;
	private BigInteger mrklRootHash;
	private BigInteger timestamp;
	private BigInteger bits;
	private BigInteger nonce;
	
	private transient volatile byte[] hash;
	private transient volatile String id;
	private transient volatile Boolean bip9;
	private transient volatile Boolean bip91;
	private transient volatile Boolean bip141;
	private transient volatile Boolean pow;
	private transient volatile BigInteger target;
	private transient volatile BigDecimal difficulty;
	
	public Block(BigInteger version, BigInteger pvBlkHash, BigInteger mrklRootHash, BigInteger timestamp, BigInteger bits, BigInteger nonce) {
		this(version, pvBlkHash, mrklRootHash, timestamp, bits, nonce, null);
	}
	
	public Block(BigInteger version, BigInteger pvBlkHash, BigInteger mrklRootHash, BigInteger timestamp, BigInteger bits, BigInteger nonce, List<byte[]> txHashes) {
		checkNull(version, pvBlkHash, mrklRootHash, timestamp, bits, nonce);
		
		if (!isNull(txHashes) && !txHashes.isEmpty() && !isValidMerkleRoot(mrklRootHash, txHashes)) {
			throw new IllegalArgumentException("Invalid merkle root");
		}
		
		this.version = version;
		this.pvBlkHash = pvBlkHash;
		this.mrklRootHash = mrklRootHash;
		this.timestamp = timestamp;
		this.bits = bits;
		this.nonce = nonce;
	}
	
	public BigInteger getTimestamp() {
		return timestamp;
	}
	
	public boolean isBip9Ready() {
		if (isNull(bip9)) {
			int v = version.intValue();
			bip9 = Boolean.valueOf(v >> 29 == 1);
		}
		
		return bip9.booleanValue();
	}
	
	public boolean isBip91Ready() {
		if (isNull(bip91)) {
			int v = version.intValue();
			bip91 = Boolean.valueOf(((v >> 4) & 1) == 1);
		}
		
		return bip91.booleanValue();
	}
	
	public boolean isBip141Ready() {
		if (isNull(bip141)) {
			int v = version.intValue();
			bip141 = Boolean.valueOf(((v >> 1) & 1) == 1);
		}
		
		return bip141.booleanValue();
	}
	
	public String getId() {
		if (id == null) {
			id = bytesToHex(getHash());
		}
		
		return id;
	}
	
	public byte[] getHash() {
		if (hash == null) {
			OutputSink sink = new OutputSink();
			this.writeTo(sink);
			byte[] h = hash256(sink.toByteArray());
			
			// The hash is the little endian of the original hash,
			// hence we need to reverse the byte array ...
			reverse(h);
			hash = h;
			
		}
		
		return hash;
	}
	
	public BigInteger getTarget() {
		if (isNull(target)) {
			target = bitsToTarget(bits);
		}
		
		return target;
	}
	
	public BigDecimal getDifficulty() {
		if (isNull(difficulty)) {
			difficulty = difficultyFromTarget(getTarget());
		}
		
		return difficulty;
	}
	
	public boolean isValidPoW() {
		if (isNull(pow)) {
			BigInteger idNum = new BigInteger(getId(), 16);
			pow = isThisLessThanThat(idNum, getTarget());
		}
		
		return pow.booleanValue();
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
	}
	
	public static Block parse(InputSource source) {
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
		
		return new Block(version, pvBlkHash, mrklRootHash, timestamp, bits, nonce);
	}
	
	private boolean isValidMerkleRoot(BigInteger merkleRoot, List<byte[]> txHashes) {		
		List<BigInteger> hashes = new ArrayList<BigInteger>(txHashes.size());
		
		// Reverse the bytes and convert to big integer ...
		for (byte[] b: txHashes) {
			reverse(b);
			hashes.add(new BigInteger(1, b));
		}
		
		BigInteger rootHash = MerkleTree.rootOf(hashes);
		byte[] hash = rootHash.toByteArray();
		
		// reverse the bytes again ...
		reverse(hash);
		
		BigInteger root = new BigInteger(1, hash);
		return areEqual(merkleRoot, root);
	}
}
