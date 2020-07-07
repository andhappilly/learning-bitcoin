package bitcoin.lang;

import static bitcoin.util.Functions.checkNull;
import static bitcoin.util.Functions.isNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import bitcoin.lang.dtype.Data;

public abstract class Executable extends Command {
	
	public static final class Context {
		private Stack<Data<?>> regStack, altStack;
		private Stack<List<Command>> comStack;
		private Stack<List<Data<?>>> witStack;
		
		Context() {}		
		
		public Stack<Data<?>> getRegularStack() {
			if (isNull(regStack)) {
				regStack = new Stack<Data<?>>();
			}
			
			return regStack;
		}
		
		public Stack<Data<?>> getAlternateStack() {
			if (isNull(altStack)) {
				altStack = new Stack<Data<?>>();
			}
			
			return altStack;
		}
		
		public List<Command> getCommands() {
			if (isNull(comStack) || comStack.isEmpty()) {
				return null;
			}
			
			return comStack.peek();
		}
		
		public List<Data<?>> getWitnesses() {
			if (isNull(witStack) || witStack.isEmpty()) {
				return null;
			}
			
			return witStack.peek();
		}
		
		void addWitnesses(List<Data<?>> witnesses) {
			checkNull(witnesses);
			
			if (isNull(witStack)) {
				witStack = new Stack<List<Data<?>>>();
			}
			
			// Create a copy of the witnesses ...
			ArrayList<Data<?>> copy = new ArrayList<Data<?>>(witnesses);
			witStack.push(copy);
		}
		
		void addCommandsFrom(Script script) {
			checkNull(script);
			
			if (isNull(comStack)) {
				comStack = new Stack<List<Command>>();
			}
			
			// Create a copy of the commands ...
			ArrayList<Command> copy = new ArrayList<Command>(script.getAllCommands());
			comStack.push(copy);
		}
		
		void removeCommands() {
			if (!isNull(comStack) && !comStack.isEmpty()) {
				comStack.pop();
			}
		}
		
		void removeWitnesses() {
			if (!isNull(witStack) && !witStack.isEmpty()) {
				witStack.pop();
			}
		}
	}
	
	public final boolean isExecutable() {
		return true;
	}
	
	public final boolean execute(Context context) {		
		if (beforeExec(context)) {
			try {
				return exec(context);
			} finally {
				afterExec(context);
			}
		}
		
		return false;
	}
	
	protected void clearAltStack(Context context) {
		// Clear any state stored on the alternate stack before execution ...
		Stack<Data<?>> altStack = context.getAlternateStack();
		if (altStack != null && !altStack.isEmpty()) {
			altStack.clear();
		}
	}
	
	protected abstract boolean beforeExec(Context context);
	protected abstract boolean exec(Context context);
	protected abstract void afterExec(Context context);
}
