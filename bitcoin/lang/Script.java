package bitcoin.lang;

import static bitcoin.util.BigInt.from;
import static bitcoin.util.BigInt.fromLittleEndian;
import static bitcoin.util.BigInt.toVarInt;
import static bitcoin.util.Bytes.concatenate;
import static bitcoin.util.Crypto.sha256;
import static bitcoin.util.Functions.areEqual;
import static bitcoin.util.Functions.checkNull;
import static bitcoin.util.Functions.isNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import bitcoin.lang.dtype.ByteArray;
import bitcoin.lang.dtype.Data;
import bitcoin.lang.dtype.NormalString;
import bitcoin.lang.op.Op0;
import bitcoin.lang.op.OpCheckSig;
import bitcoin.lang.op.OpCode;
import bitcoin.lang.op.OpDup;
import bitcoin.lang.op.OpEqual;
import bitcoin.lang.op.OpEqualVerify;
import bitcoin.lang.op.OpHash160;
import bitcoin.lang.op.OpVerify;
import bitcoin.util.InputSource;
import bitcoin.util.OutputSink;

public final class Script extends Executable {
	
	private List<Command> commands;
	
	private transient volatile String rep;
	
	private transient volatile Boolean p2pkh;
	
	private transient volatile Boolean p2sh;
	
	private transient volatile Boolean p2wpkh;
	
	private transient volatile Boolean p2wsh;
	
	public Script(List<Command> commands) {
		checkNull(commands);
		
		this.commands = Collections.unmodifiableList(commands);
	}
	
	public List<Command> getAllCommands() {
		return commands;
	}
	
	public Command getCommand(int index) {
		if (index < 0 || index > commands.size() - 1) {
			throw new IllegalArgumentException("No command with that index exists.");
		}
		
		return commands.get(index);
	}
	
	public Command getFirstCommand() {
		return getCommand(0);
	}
	
	public Command getLastCommand() {		
		return getCommand(commands.size() - 1);
	}
	
	public void writeTo(OutputSink sink) {
		checkNull(sink);
		
		for(Command c: commands) {
			c.writeTo(sink);
		}
	}
	
	public String toString() {
		if (rep == null) {
			StringBuilder buffer = new StringBuilder();
			for (Command c: commands) {
				buffer.append(c);				
				buffer.append(" ");
			}
			
			buffer.setLength(buffer.length() - 1);			
			rep = buffer.toString();
		}		
		
		return rep;
	}
	
	public boolean equals(Object other) {
		if (other instanceof Script) {
			Script another = (Script)other;
			return areEqual(this.commands, another.commands);
		}
		
		return false;
	}
	
	public boolean isP2PKH() {
		if (isNull(p2pkh)) {
			boolean p = commands.size() == 5;
			p = OpDup.INSTANCE.equals(commands.get(0));
			p = p && OpHash160.INSTANCE.equals(commands.get(1));
			p = p && commands.get(2) instanceof ByteArray;
			p = p && OpEqualVerify.INSTANCE.equals(commands.get(3));
			p = p && OpCheckSig.INSTANCE.equals(commands.get(4));
			
			if (p) {
				byte[] data = ((ByteArray)commands.get(2)).read();
				p = data.length == 20;
			}
			
			p2pkh = Boolean.valueOf(p);
		}
		
		return p2pkh.booleanValue();
	}
	
	public boolean isP2SH() {
		if (isNull(p2sh)) {
			boolean p = commands.size() == 3;
			p = OpHash160.INSTANCE.equals(commands.get(0));
			p = p && commands.get(1) instanceof ByteArray;
			p = p && OpEqual.INSTANCE.equals(commands.get(2));
			
			if (p) {
				byte[] data = ((ByteArray)commands.get(1)).read();
				p = data.length == 20;
			}
			
			p2sh = Boolean.valueOf(p);
		}
		
		return p2sh.booleanValue();
	}
	
	public boolean isP2WPKH() {
		if (isNull(p2wpkh)) {
			boolean p = commands.size() == 2;
			p = Op0.INSTANCE.equals(commands.get(0));
			p = p && commands.get(1) instanceof ByteArray;
			
			if (p) {
				byte[] data = ((ByteArray)commands.get(1)).read();
				p = data.length == 20;
			}
			
			p2wpkh = Boolean.valueOf(p);
		}
		
		return p2wpkh.booleanValue();
	}
	
	public boolean isP2WSH() {
		if (isNull(p2wsh)) {
			boolean p = commands.size() == 2;
			p = Op0.INSTANCE.equals(commands.get(0));
			p = p && commands.get(1) instanceof ByteArray;
			
			if (p) {
				byte[] data = ((ByteArray)commands.get(1)).read();
				p = data.length == 32;
			}
			
			p2wsh = Boolean.valueOf(p);
		}
		
		return p2wsh.booleanValue();
	}	
	
	public Script add(Script another) {
		checkNull(another);		
		
		List<Command> instructions = new ArrayList<Command>(commands.size() + 1);
		instructions.add(another);		
		instructions.addAll(commands);
		
		return new Script(instructions);
	}
	
	public boolean evaluate(Data<?> ... data) {		
		return compute(null, data);
	}
	
	public boolean evaluate(List<Data<?>> witnesses, Data<?> ... data) {
		checkNull(witnesses);
		
		return compute(witnesses, data);
	}	
	
	public static Script parse(InputSource source) {
		checkNull(source);
		
		List<Command> commands = new ArrayList<Command>();
		int n = -1;
		while ((n = source.readNextByte(false)) != -1 ) {
			n = Byte.toUnsignedInt((byte)n);
			if (n >= 1 && n <= 75) {				
				ByteArray data = new ByteArray(source.readNextBytes(n));
				commands.add(data);
			} else if (n == 76) {
				int dataLength = source.readNextByte();
				ByteArray data = new ByteArray(source.readNextBytes(dataLength));
				commands.add(data);
			} else if (n == 77) {
				int dataLength = fromLittleEndian(source.readNextBytes(2)).intValue();
				ByteArray data = new ByteArray(source.readNextBytes(dataLength));
				commands.add(data);
			} else {				
				OpCode instruction = OpCode.from(n);
				if (isNull(instruction)) {
					throw new IllegalArgumentException("Opcode '"+n+"' not valid");
				}				
				commands.add(instruction);
			}
		}		
		
		return new Script(commands);
	}
	
	public static Script fromData(Data<?> data) {
		byte[] redeemBytes = data.readAsBytes();
		byte[] varIntLength = toVarInt(from(redeemBytes.length));
		byte[] scriptBytes = concatenate(varIntLength, redeemBytes);
		return Script.parse(InputSource.wrap(scriptBytes));
	}
	
	protected final boolean beforeExec(Context context) {
		// Clear any state stored on the alternate stack before execution ...
		clearAltStack(context);
		
		// Add all the commands associated with this script into the context ...
		context.addCommandsFrom(this);
		return true;
	}
	
	protected boolean exec(Context context) {
		// The copy of the script commands in the context are mutable as opposed to 
		// commands in the script ...
		List<Command> commands = context.getCommands();
		Stack<Data<?>> inputs = context.getRegularStack();
		//Debug.printStack(inputs);
		//for (Command c: commands) {		
		int i = 0;
		while (i < commands.size()) {
			Command c = commands.get(i);
			if (c.isExecutable()) {
				Executable e = (Executable)c;
				if (e.execute(context)) {
					++i;
					continue;
				}
				
				return false;
			}
			
			Data<?> d = (Data<?>)c;
			inputs.add(d);
			// This is a special handling for the p2sh scenario ...
			int remainingCommands = commands.size() - (i + 1);
			if (remainingCommands == 3 && 
					OpHash160.INSTANCE.equals(commands.get(i + 1)) &&
						commands.get(i + 2) instanceof ByteArray && 
							((ByteArray)commands.get(i + 2)).read().length == 20 &&
								OpEqual.INSTANCE.equals(commands.get(i + 3))) {
				ByteArray h160 = ((ByteArray)commands.get(i + 2));
				if (!OpHash160.INSTANCE.execute(context)) {
					return false;
				}
				
				inputs.add(h160);
				
				if (!OpEqual.INSTANCE.execute(context)) {
					return false;
				}
				
				if (!OpVerify.INSTANCE.execute(context)) {
					return false;
				}				
				
				Script redeemScript = fromData(d);
				commands.addAll(redeemScript.commands);
				
				i = i + 3;
			} else {
				++i;
			} 
			
			// Special handling for p2wkh scenario ...
			if (inputs.size() == 2 && 
				NormalString.FAILURE.equals(inputs.get(0)) && 
					inputs.get(1) instanceof ByteArray && 
						((ByteArray)inputs.get(1)).read().length == 20) {
				ByteArray h160 = (ByteArray)inputs.pop();	
				inputs.pop();
				
				List<Data<?>> witnesses = context.getWitnesses();
				if (!isNull(witnesses)) {
					commands.addAll(witnesses);
				}				
				
				commands.addAll(new Script(Arrays.asList(h160)).getAllCommands());
			}
			
			// Special handling for p2wsh scenario ...
			if (inputs.size() == 2 && 
				NormalString.FAILURE.equals(inputs.get(0)) && 
					inputs.get(1) instanceof ByteArray && 
						((ByteArray)inputs.get(1)).read().length == 32) {
				byte[] s256 = ((ByteArray)inputs.pop()).read();	
				inputs.pop();
				
				List<Data<?>> witnesses = context.getWitnesses();
				if (!isNull(witnesses)) {
					int last = witnesses.size() - 1;
					for (int k = 0; i < last; ++k) {
						Data<?> witness = witnesses.get(k);
						commands.add(witness);
					}
					
					Data<?> witnessData = witnesses.get(last);
					byte[] scriptBytes = witnessData.readAsBytes();
					if (!Arrays.equals(s256, sha256(scriptBytes))) {
						return false;
					}
					
					Script witnessScript = Script.fromData(witnessData);
					commands.addAll(witnessScript.getAllCommands());
				}
			}
		}
		
		if (inputs.isEmpty()) {
			return false;
		}
		
		Data<?> end = inputs.peek();	
		if (end instanceof NormalString) {
			return !areEqual(NormalString.FAILURE, end);
		}
		
		return true;
	}
	
	protected final void afterExec(Context context) {
		// Clear any state stored on the alternate stack after execution ...
		clearAltStack(context);
		
		// Clear all the current commands from the context ...
		context.removeCommands();
		
		// Clear all the current witnesses from the context ...
		context.removeWitnesses();
	}
	
	private boolean compute(List<Data<?>> witnesses, Data<?> ... data) {
		Context context = new Context();
		
		if (!isNull(witnesses)) {
			context.addWitnesses(witnesses);
		}
		
		for (Data<?> d: data) {
			Stack<Data<?>> regStack = context.getRegularStack();
			regStack.push(d);
		}
		
		return execute(context);
	}
}
