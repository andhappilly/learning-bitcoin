package bitcoin.core;

import static bitcoin.core.TxFetcher.fetch;
import static bitcoin.crypto.ecc.Signature.SIGHASH_ALL;
import static bitcoin.crypto.ecc.Signature.SIGHASH_ALL_MARKER;
import static bitcoin.util.BigInt.from;
import static bitcoin.util.BigInt.fromLittleEndian;
import static bitcoin.util.BigInt.fromVarInt;
import static bitcoin.util.BigInt.getVarIntSize;
import static bitcoin.util.BigInt.isThisLessThanThat;
import static bitcoin.util.BigInt.toLittleEndian;
import static bitcoin.util.BigInt.toVarInt;
import static bitcoin.util.Bytes.ONE;
import static bitcoin.util.Bytes.ZERO;
import static bitcoin.util.Bytes.bytesToHex;
import static bitcoin.util.Bytes.hexCharsToByte;
import static bitcoin.util.Bytes.hexToBytes;
import static bitcoin.util.Bytes.reverse;
import static bitcoin.util.Crypto.hash256;
import static bitcoin.util.Crypto.hash256BigInt;
import static bitcoin.util.Functions.areEqual;
import static bitcoin.util.Functions.checkNull;
import static bitcoin.util.Functions.isNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import bitcoin.crypto.ecc.Secret;
import bitcoin.lang.Command;
import bitcoin.lang.Script;
import bitcoin.lang.dtype.ByteArray;
import bitcoin.lang.dtype.Data;
import bitcoin.lang.dtype.HexString;
import bitcoin.util.BytesEncodeable;
import bitcoin.util.Functions;
import bitcoin.util.InputSource;
import bitcoin.util.OutputSink;

public final class Transaction extends BytesEncodeable {	
	private static final BigInteger NO_INPUT_INDEX = new BigInteger("ffffffff", 16);
	private static final String NEW_LINE = System.lineSeparator();
	
	public static final class Input extends BytesEncodeable {
		private static final BigInteger DEFAULT_SEQUENCE = new BigInteger("ffffffff", 16);
				
		private BigInteger pvTxHash;
		private BigInteger pvTxIndex;
		private Script scriptSig;
		private BigInteger seq;
		
		private List<Data<?>> witnesses;
		
		private transient volatile String rep;		
		
		public Input(BigInteger pvTxHash, BigInteger pvTxIndex, Script scriptSig, BigInteger seq) {
			checkNull(pvTxHash, pvTxIndex);
			
			this.pvTxHash = pvTxHash;
			this.pvTxIndex = pvTxIndex;
			this.scriptSig = scriptSig;
			this.seq = isNull(seq) ? DEFAULT_SEQUENCE : seq;
		}
		
		public BigInteger getPrevTxnHash() {
			return pvTxHash;
		}
		
		public BigInteger getPrevTxnIndex() {
			return pvTxIndex;
		}
		
		public Script getScriptSig() {
			return scriptSig;
		}
		
		public BigInteger getSeq() {
			return seq;
		}
		
		public boolean equals(Object another) {
			if (another instanceof Input) {
				Input other = (Input)another;
				
				boolean eq = Functions.areEqual(this.pvTxHash, other.pvTxHash);
				eq = eq && Functions.areEqual(this.pvTxIndex, other.pvTxIndex);
				eq = eq && Functions.areEqual(this.scriptSig, other.scriptSig);
				eq = eq && Functions.areEqual(this.seq, other.seq);
				
				return eq;
			}
			
			return false;
		}
		
		public String toString() {
			if (rep == null) {
				StringBuilder buffer = new StringBuilder();
				
				buffer.append(pvTxHash.toString(16)).append(":").append(pvTxIndex);
				
				rep = buffer.toString();
			}
			
			return rep;
		}
		
		public void writeTo(OutputSink sink) {
			checkNull(sink);
			
			// Convert the previous transaction hash into little endian bytes and write to the sink ...
			byte[] bytes = toLittleEndian(pvTxHash, 32);
			sink.write(bytes);
			
			// Convert the previous transaction index into little endian bytes and write to the sink ...
			bytes = toLittleEndian(pvTxIndex, 4);
			sink.write(bytes);
			
			// Write scriptsig to the sink ...
			if (isNull(scriptSig)) {
				sink.write(ZERO);
			} else {
				OutputSink localSink = new OutputSink();				
				scriptSig.writeTo(localSink);
				
				byte[] scriptBytes = localSink.toByteArray();
				byte[] scriptLength = toVarInt(from(scriptBytes.length));
				
				sink.write(scriptLength);
				sink.write(scriptBytes);
			}
			
			// Lastly write the sequence to the sink ...
			bytes = toLittleEndian(seq, 4);
			sink.write(bytes);
		}
		
		public static Input parse(InputSource source) {
			checkNull(source);
			
			// Input starts with the previous transaction hash and that is 32 bytes (64 characters) ...
			char[] chars = source.readNextChars(64);
			byte[] bytes = hexToBytes(chars);
			BigInteger pvTxHash = fromLittleEndian(bytes);
			
			// Next read previous transaction index and that is 4 bytes (8 characters) ...
			chars = source.readNextChars(8);
			bytes = hexToBytes(chars);
			BigInteger pvTxIndex = fromLittleEndian(bytes);
			
			// Next to parse is the "scriptsig" field ...
			// The "scriptsig" field is of variable length, hence need to read the next byte
			// to determine the size ..
			chars = source.readNextChars(2);
			byte b = hexCharsToByte(chars[0], chars[1]);
			int vIntSize = getVarIntSize(b);		
			
			BigInteger scriptSigSize;
			if (vIntSize == 1) {
				// If the variable integer size is 1, the already read byte represents the size ...
				scriptSigSize = BigInteger.valueOf(b);
			} else {
				// Read the next 2*vIntSize characters that represent the size ...
				chars = source.readNextChars(2*vIntSize);
				bytes = hexToBytes(chars);
				scriptSigSize = fromVarInt(bytes);
			}
			
			Script scriptSig = null;
			if (!areEqual(BigInteger.ZERO, scriptSigSize)) {			
				// Read the next 2*scriptSigSize characters that represent script sig hex ...
				chars = source.readNextChars(BigInteger.TWO.multiply(scriptSigSize).intValue());
				bytes = hexToBytes(chars);			
				scriptSig = Script.parse(InputSource.wrap(bytes));
			}
			
			// Lastly read the sequence and that is 4 bytes (8 characters) ...
			chars = source.readNextChars(8);
			bytes = hexToBytes(chars);
			BigInteger seq = fromLittleEndian(bytes);
			
			return new Input(pvTxHash, pvTxIndex, scriptSig, seq);
		}
		
		private void setWitnesses(List<Data<?>> witnesses) {
			this.witnesses = witnesses;
		}
		
		private List<Data<?>> getWitnesses() {
			return witnesses;
		}
		
		private Data<?> getLastWitness() {
			return isNull(witnesses) || witnesses.isEmpty() ? 
						null : witnesses.get(witnesses.size() - 1);
		}
		
		private Transaction fetchPrevTxn(boolean testnet) {
			return fetch(pvTxHash, testnet, false);
		}
		
		private Output fetchPrvTxnOutput(boolean testnet) {
			Transaction prevTxn = fetchPrevTxn(testnet);
			List<Output> outputs = prevTxn.getOutputs();
			
			return outputs.get(pvTxIndex.intValue());
		}
		
		private BigInteger value(boolean testnet) {
			Output output = fetchPrvTxnOutput(testnet);
			return output.getAmount();
		}
		
		private Script scriptPubKey(boolean testnet) {
			Output output = fetchPrvTxnOutput(testnet);
			return output.getScriptPubKey();
		}
	}
	
	public static final class Output extends BytesEncodeable {
		private BigInteger amount;
		private Script scriptPubKey;
		
		private transient volatile String rep;
		
		public Output(BigInteger amount, Script scriptPubKey) {
			checkNull(amount, scriptPubKey);
			
			this.amount = amount;
			this.scriptPubKey = scriptPubKey;
		}
		
		public BigInteger getAmount() {
			return amount;
		}
		
		public Script getScriptPubKey() {
			return scriptPubKey;
		}
		
		public String toString() {
			if (rep == null) {
				StringBuilder buffer = new StringBuilder();
				
				buffer.append(amount).append(":").append(scriptPubKey);
				
				rep = buffer.toString();
			}
			
			return rep;
		}
		
		public boolean equals(Object another) {
			if (another instanceof Output) {
				Output other = (Output)another;
				
				boolean eq = Functions.areEqual(this.amount, other.amount);
				eq = eq && Functions.areEqual(this.scriptPubKey, other.scriptPubKey);
				
				return eq;
			}
			
			return false;
		}
		
		public void writeTo(OutputSink sink) {
			checkNull(sink);
			
			// Convert the amount into little endian bytes and write to the sink ...
			byte[] bytes = toLittleEndian(amount, 8);
			sink.write(bytes);
			
			// Now write the ScriptPubKey to the sink ...
			OutputSink localSink = new OutputSink();				
			scriptPubKey.writeTo(localSink);
			
			byte[] scriptBytes = localSink.toByteArray();
			byte[] scriptLength = toVarInt(from(scriptBytes.length));
			
			sink.write(scriptLength);
			sink.write(scriptBytes);
		}
		
		public static Output parse(InputSource source) {
			checkNull(source);
			
			// Read the next 16 characters (8 bytes) that represent the amount ...
			char[] chars = source.readNextChars(16);
			byte[] bytes = hexToBytes(chars);			
			BigInteger amount = fromLittleEndian(bytes);
			
			// Next to parse is the "scriptPubKey" field ...
			// The "scriptPubKey" field is of variable length, hence need to read the next byte
			// to determine the size ..
			chars = source.readNextChars(2);
			byte b = hexCharsToByte(chars[0], chars[1]);
			int vIntSize = getVarIntSize(b);		
			
			BigInteger scriptPubKeySize;
			if (vIntSize == 1) {
				// If the variable integer size is 1, the already read byte represents the size ...
				scriptPubKeySize = BigInteger.valueOf(b);
			} else {
				// Read the next 2*vIntSize characters that represent the size ...
				chars = source.readNextChars(2*vIntSize);
				bytes = hexToBytes(chars);
				scriptPubKeySize = fromVarInt(bytes);
			}
			
			// Read the next 2*scriptPubKeySize characters that represent scriptPubKey hex ...
			chars = source.readNextChars(BigInteger.TWO.multiply(scriptPubKeySize).intValue());
			bytes = hexToBytes(chars);
			Script scriptPubKey = Script.parse(InputSource.wrap(bytes));
			
			return new Output(amount, scriptPubKey);
		}
	}
	
	private BigInteger version;
	private List<Input> inputs;
	private List<Output> outputs;
	private BigInteger lockTime;
	private boolean coinbase;
	private BigInteger coinbaseHeight;
	private boolean testnet;
	private boolean segwit;
	
	private transient volatile byte[] hash;
	private transient volatile String id;
	private transient volatile String rep;
	private transient volatile BigInteger fee;
	
	private transient volatile byte[] outputs_hash;
	private transient volatile byte[] seqs_hash;
	private transient volatile byte[] prevTxs_hash;
	
	public Transaction(BigInteger version, List<Input> inputs, List<Output> outputs, BigInteger lockTime) {
		this(version, inputs, outputs, lockTime, false);
	}
	
	public Transaction(BigInteger version, List<Input> inputs, List<Output> outputs, BigInteger lockTime, boolean segwit) {
		this(version, inputs, outputs, lockTime, false, segwit);
	}
	
	public Transaction(BigInteger version, List<Input> inputs, List<Output> outputs, BigInteger lockTime, boolean testnet, boolean segwit) {
		checkNull(version, inputs, outputs, lockTime);
		
		this.version = version;
		this.inputs = inputs;
		this.outputs = outputs;
		this.lockTime = lockTime;
		this.testnet = testnet;
		this.segwit = segwit;
		this.coinbase = 
				inputs.size() == 1 &&
					areEqual(BigInteger.ZERO, inputs.get(0).pvTxHash) &&
						areEqual(NO_INPUT_INDEX, inputs.get(0).pvTxIndex);
		
		if (coinbase) {
			Data<?> d = (Data<?>)inputs.get(0).scriptSig.getFirstCommand();
			this.coinbaseHeight = fromLittleEndian(d.readAsBytes());
		}
	}
	
	public String toString() {
		if (rep == null) {
			StringBuilder inputs_buffer = new StringBuilder();
			for(Input input:inputs) {
				inputs_buffer.append(input).append(NEW_LINE);
			}
			
			StringBuilder outputs_buffer = new StringBuilder();
			for(Output output:outputs) {
				outputs_buffer.append(output).append(NEW_LINE);
			}
			
			StringBuilder buffer = new StringBuilder();
			buffer.append("tx: ").append(getId()).append(NEW_LINE);
			buffer.append("version: ").append(version).append(NEW_LINE);
			buffer.append("tx_ins: ").append(inputs_buffer).append(NEW_LINE);
			buffer.append("tx_outs: ").append(outputs_buffer).append(NEW_LINE);
			buffer.append("locktime: ").append(lockTime).append(NEW_LINE);
			
			rep = buffer.toString();
		}
		
		return rep;
	}
	
	public boolean equals(Object another) {
		if (another instanceof Transaction) {
			Transaction other = (Transaction)another;
			
			boolean eq = Functions.areEqual(this.version, other.version);
			eq = eq && Functions.areEqual(this.inputs, other.inputs);
			eq = eq && Functions.areEqual(this.outputs, other.outputs);
			eq = eq && Functions.areEqual(this.lockTime, other.lockTime);
			
			return eq;
		}
		
		return false;
	}
	
	public boolean isCoinbase() {
		return coinbase;
	}
	
	public BigInteger getCoinbaseHeight() {
		return coinbaseHeight;
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
			this.writeTo(sink, true);
			byte[] h = hash256(sink.toByteArray());
			
			// The hash is the little endian of the original hash,
			// hence we need to reverse the byte array ...
			reverse(h);
			hash = h;
			
		}
		
		return hash;
	}
	
	public BigInteger getVersion() {
		return version;
	}
	
	public List<Input> getInputs() {
		return inputs;
	}
	
	public List<Output> getOutputs() {
		return outputs;
	}
	
	public BigInteger getLockTime() {
		return lockTime;
	}
	
	public BigInteger getInputHash(int index) {
		return getInputHash(index, null);
	}
	
	public BigInteger getInputHash(int index, Script redeemScript) {
		int inputSize = inputs.size();
		List<Input> modInputs = new ArrayList<Input>(inputSize);
		for (int i = 0; i < inputSize; ++i) {
			Input input = inputs.get(i);
			Input modInput;
			if (i == index) {				
				// Replace sigscript with scriptpubkey ...
				Script script = isNull(redeemScript) ? input.scriptPubKey(testnet) : redeemScript;
				modInput = new Input(input.pvTxHash, input.pvTxIndex, script, input.seq);
			} else {
				modInput = new Input(input.pvTxHash, input.pvTxIndex, null, input.seq);
			}
			
			modInputs.add(modInput);
		}
		
		Transaction modTx = new Transaction(version, modInputs, outputs, lockTime);
		OutputSink sink = new OutputSink();
		modTx.writeTo(sink);
		
		// Finally write the SIGHASH_ALL value to the sink ...
		byte[] bytes = toLittleEndian(SIGHASH_ALL, 4);
		sink.write(bytes);
		
		// Do hash256 over the serialized version of the modified transaction
		return hash256BigInt(sink.toByteArray());
	}
	
	public BigInteger getSegwitInputHash(int index) {
		return getSegwitInputHash(index, null, null);
	}
	
	public BigInteger getSegwitInputHash(int index, Script redeemScript) {
		return getSegwitInputHash(index, redeemScript, null);
	}
	
	public BigInteger getSegwitInputHash(int index, Script redeemScript, Script witnessScript) {
		Input input = inputs.get(index);
		OutputSink sink = new OutputSink();
		
		sink.write(toLittleEndian(version, 4));
		sink.write(getPrevTxsHash());
		sink.write(getSeqsHash());
		
		byte[] prevTx = input.pvTxHash.toByteArray();
		reverse(prevTx);
		
		sink.write(prevTx);
		sink.write(toLittleEndian(input.pvTxIndex, 4));
		
		if (!isNull(witnessScript)) {
			witnessScript.writeTo(sink);
		} else if (!isNull(redeemScript)) {
			Script p2pkh = new Script(Arrays.asList(redeemScript.getCommand(1)));
			p2pkh.writeTo(sink);
		} else {
			Script p2pkh = new Script(Arrays.asList(input.scriptPubKey(testnet).getCommand(1)));
			p2pkh.writeTo(sink);
		}
		
		sink.write(toLittleEndian(input.value(testnet), 8));
		sink.write(toLittleEndian(input.seq, 4));
		sink.write(getOutputsHash());
		sink.write(toLittleEndian(lockTime, 4));
		
		byte[] bytes = toLittleEndian(SIGHASH_ALL, 4);
		sink.write(bytes);		
		
		return hash256BigInt(sink.toByteArray());
	}
	
	public boolean signInput(int index, Secret secret) {
		BigInteger z = getInputHash(index);
		String der = secret.sign(z).toDER();
		der = der + SIGHASH_ALL_MARKER; 
		String pubKey = secret.pubKey().toSEC();
		
		List<Command> commands = new ArrayList<Command>(2);
		commands.add(new HexString(der));
		commands.add(new HexString(pubKey));
		
		Input input = inputs.get(index);
		input.scriptSig = new Script(commands);
		return verifyInput(index);
	}
	
	public boolean verifyInput(int index) {
		Input input = inputs.get(index);
		Script scriptPubKey = input.scriptPubKey(testnet);
		Script scriptSig = input.getScriptSig();
		ByteArray zVal;
		List<Data<?>> witnesses = null;
		if (scriptPubKey.isP2SH()) {			
			Script redeemScript = 
					Script.fromData((Data<?>)scriptSig.getLastCommand());
			if (redeemScript.isP2WPKH()) {	
				witnesses = input.getWitnesses();
				BigInteger z = getSegwitInputHash(index, redeemScript);
				zVal = new ByteArray(z.toByteArray());
			} else if (redeemScript.isP2WSH()) {
				witnesses = input.getWitnesses();
				Script witnessScript = 
						Script.fromData(input.getLastWitness());
				BigInteger z = getSegwitInputHash(index, null, witnessScript);
				zVal = new ByteArray(z.toByteArray());
			} else {	
				BigInteger z = getInputHash(index, redeemScript);
				zVal = new ByteArray(z.toByteArray());
			}
		} else {
			if (scriptPubKey.isP2WPKH()) {
				witnesses = input.getWitnesses();
				BigInteger z = getSegwitInputHash(index);				
				zVal = new ByteArray(z.toByteArray());
			} else if (scriptPubKey.isP2WSH()) {
				witnesses = input.getWitnesses();
				Script witnessScript = 
						Script.fromData(input.getLastWitness());
				BigInteger z = getSegwitInputHash(index, null, witnessScript);
				zVal = new ByteArray(z.toByteArray());
			} else {				
				BigInteger z = getInputHash(index);
				zVal = new ByteArray(z.toByteArray());
			}
		}
				
		Script verifier = scriptPubKey.add(scriptSig);
		return isNull(witnesses) ? verifier.evaluate(zVal) : verifier.evaluate(witnesses, zVal);
	}
	
	public boolean verify() {
		BigInteger fee = getFee();
		if (isThisLessThanThat(fee, BigInteger.ZERO)) {
			return false;
		}
		
		int inputSize = inputs.size();
		for (int i = 0; i < inputSize; ++i) {
			if (!verifyInput(i)) {
				return false;
			}
		}
		
		return true;
	}
	
	public BigInteger getFee() {
		if (isNull(fee)) {
			BigInteger inputTotal = BigInteger.ZERO;
			for(Input input: inputs) {
				inputTotal.add(input.value(testnet));
			}
			
			BigInteger outputTotal = BigInteger.ZERO;
			for(Output output: outputs) {
				outputTotal.add(output.getAmount());
			}
			
			fee = inputTotal.subtract(outputTotal);
		}
		
		return fee;
	}
	
	public void writeTo(OutputSink sink) {
		writeTo(sink, false);
	}
	
	public static Transaction parse(InputSource source) {		
		checkNull(source);
		
		// Parse the version first ...
		// Read the version bytes (4 bytes or 8 characters) first ...
		char[] chars = source.readNextChars(8);
		byte[] bytes = hexToBytes(chars);
		
		BigInteger version = fromLittleEndian(bytes);
		
		// Check for the segwit marker ...
		chars = source.readNextChars(2);
		byte b = hexCharsToByte(chars[0], chars[1]);
		boolean segwit = ZERO == b;
		if (segwit) {
			// If the segwit marker is present check if the flag is set ...
			chars = source.readNextChars(2);
			b = hexCharsToByte(chars[0], chars[1]);
			
			if (ONE != b) {
				throw new IllegalArgumentException("Invalid segwit transaction.");
			}
			
			// Read the next 2 characters that will tell us the number of inputs expected ...			
			chars = source.readNextChars(2);
			b = hexCharsToByte(chars[0], chars[1]);
		}		
		
		// If it is not segwit the already read byte should give us the number of inputs ...
		int vIntSize = getVarIntSize(b);		
		
		BigInteger totalInputCount;
		if (vIntSize == 1) {
			// If the variable integer size is 1, the already read byte represents the 
			// number of inputs ...
			totalInputCount = BigInteger.valueOf(b);
		} else {
			// Read the next 2*vIntSize characters represent the number of inputs ...
			chars = source.readNextChars(2*vIntSize);
			bytes = hexToBytes(chars);
			totalInputCount = fromVarInt(bytes);
		}
		
		List<Input> inputs = new ArrayList<Input>(totalInputCount.intValue()); 
		
		// Now we need to iterate and parse all the inputs ...
		BigInteger readInputCount = BigInteger.ZERO;
		while (isThisLessThanThat(readInputCount, totalInputCount)) {
			// Parse the input ...
			Input input = Input.parse(source);
						
			// Add this input to the set of inputs ...
			inputs.add(input);
			
			// Increment the input count ...
			readInputCount = readInputCount.add(BigInteger.ONE);
		}
		
		// Created an immutable set of inputs ...
		inputs = Collections.unmodifiableList(inputs);
		
		// Read the next 2 characters that will tell us the number of outputs expected
		// in terms of a variable integer ...
		chars = source.readNextChars(2);
		b = hexCharsToByte(chars[0], chars[1]);
		vIntSize = getVarIntSize(b);		
		
		BigInteger totalOutputCount;
		if (vIntSize == 1) {
			// If the variable integer size is 1, the already read byte represents the 
			// number of outputs ...
			totalOutputCount = BigInteger.valueOf(b);
		} else {
			// Read the next 2*vIntSize characters represent the number of outputs ...
			chars = source.readNextChars(2*vIntSize);
			bytes = hexToBytes(chars);
			totalOutputCount = fromVarInt(bytes);
		}
		
		List<Output> outputs = new ArrayList<Output>(totalInputCount.intValue()); 
		
		// Now we need to iterate and parse all the outputs ...
		BigInteger readOutputCount = BigInteger.ZERO;
		while (isThisLessThanThat(readOutputCount, totalOutputCount)) {
			// Parse the output ...
			Output output = Output.parse(source);
						
			// Add this output to the set of outputs ...
			outputs.add(output);
			
			// Increment the output count ...
			readOutputCount = readOutputCount.add(BigInteger.ONE);
		}
		
		// Created an immutable set of outputs ...
		outputs = Collections.unmodifiableList(outputs);
		
		// if this is a segwit transaction, we need to get the witness for each of the inputs ...
		if (segwit) {
			for (Input input: inputs) {
				// Read the next 2 characters that will tell us the number of witnesses for this input 
				// as a varint ...			
				chars = source.readNextChars(2);
				b = hexCharsToByte(chars[0], chars[1]);
				vIntSize = getVarIntSize(b);
				
				BigInteger witnessCount;
				if (vIntSize == 1) {
					// If the variable integer size is 1, the already read byte represents the count ...
					witnessCount = BigInteger.valueOf(b);
				} else {
					// Read the next 2*vIntSize characters represent the count ...
					chars = source.readNextChars(2*vIntSize);
					bytes = hexToBytes(chars);
					witnessCount = fromVarInt(bytes);
				}
				
				int witnessNum = witnessCount.intValue();
				List<Data<?>> witnesses = new ArrayList<Data<?>>();
				for (int i= 0 ; i < witnessNum; ++i) {
					// Read the next 2 characters that will tell us the length of witness data as a varint ...			
					chars = source.readNextChars(2);
					b = hexCharsToByte(chars[0], chars[1]);
					vIntSize = getVarIntSize(b);
					
					BigInteger witnessLength;
					if (vIntSize == 1) {
						// If the variable integer size is 1, the already read byte represents the length ...
						witnessLength = BigInteger.valueOf(b);
					} else {
						// Read the next 2*vIntSize characters represent the length ...
						chars = source.readNextChars(2*vIntSize);
						bytes = hexToBytes(chars);
						witnessLength = fromVarInt(bytes);
					}
					
					int dataLength = witnessLength.intValue();
					ByteArray data;
					if (dataLength == 0) {
						data = new ByteArray(new byte[] {ZERO});
					} else {
						// Read 2*dataLength characters and convert to bytes representing the data ...
						chars = source.readNextChars(2*dataLength);
						bytes = hexToBytes(chars);
						data = new ByteArray(bytes);
					}
					
					witnesses.add(data);
				}
				
				input.setWitnesses(witnesses);
			}
		}
		
		// Next read the locktime field that is 4 bytes (8 characters) ...
		chars = source.readNextChars(8);
		bytes = hexToBytes(chars);
		BigInteger lockTime = fromLittleEndian(bytes);
		
		return new Transaction(version, inputs, outputs, lockTime, segwit);
	} 
	
	private byte[] getPrevTxsHash() {
		if (isNull(prevTxs_hash)) {
			OutputSink txs_sink = new OutputSink();
			OutputSink seqs_sink = new OutputSink();
			for(Input i: inputs) {
				byte[] prevTx = i.pvTxHash.toByteArray();
				reverse(prevTx);
				
				txs_sink.write(prevTx);
				txs_sink.write(toLittleEndian(i.pvTxIndex, 4));
				
				seqs_sink.write(toLittleEndian(i.seq, 4));
			}
			
			this.seqs_hash = hash256(seqs_sink.toByteArray());
			this.prevTxs_hash = hash256(txs_sink.toByteArray());
		}
		
		return prevTxs_hash;
	}
	
	private byte[] getSeqsHash() {
		if (isNull(seqs_hash)) {
			// This call will calculate sequences hash as well ...
			getPrevTxsHash();
		}
		
		return seqs_hash;
	}
	
	private byte[] getOutputsHash() {
		if (isNull(outputs_hash)) {
			OutputSink sink = new OutputSink();
			for (Output o: outputs) {
				o.writeTo(sink);
			}
			
			outputs_hash = hash256(sink.toByteArray());
		}
		
		return outputs_hash;
	}
	
	private void writeTo(OutputSink sink, boolean ignoreSegwit) {
		checkNull(sink);
		
		// First write the version ...
		byte[] bytes = toLittleEndian(version, 4);
		sink.write(bytes);
		
		if (!ignoreSegwit && segwit) {
			sink.write(ZERO);
			sink.write(ONE);
		}
		
		// next write the number of inputs as a varint ...
		int inputCount = inputs.size();
		bytes = toVarInt(from(inputCount));
		sink.write(bytes);
		
		// Next write each of the inputs to the sink ...
		for(Input input: inputs) {
			input.writeTo(sink);
		}
		
		// next write the number of outputs as a varint ...
		int outputCount = outputs.size();
		bytes = toVarInt(from(outputCount));
		sink.write(bytes);
		
		// Next write each of the outputs to the sink ...
		for(Output output: outputs) {
			output.writeTo(sink);
		}
		
		// For segwit transactions we have to write the witness content next ...
		if (!ignoreSegwit && segwit) {
			// For each input, write the number of witnesses as a varint
			// followed by the witness information itself ...
			for(Input input: inputs) {
				List<Data<?>> witnesses = input.getWitnesses();
				
				// Write the number of witnesses ...
				if (isNull(witnesses)) {
					bytes = toVarInt(BigInteger.ZERO);
					sink.write(bytes);
				} else {
					bytes = toVarInt(from(witnesses.size()));
					sink.write(bytes);
					
					for (Data<?> witness: witnesses) {
						byte[] content = witness.readAsBytes();
						
						// First write the length of the content as a varint ...
						bytes = toVarInt(from(content.length));
						sink.write(bytes);
						
						// Now write the content ...
						sink.write(content);
					}
				}
			}
		}
		
		// Finally write the lock time to the sink ...
		bytes = toLittleEndian(lockTime, 4);
		sink.write(bytes);
	}
}
