package bitcoin.util;

import java.util.Enumeration;
import java.util.Stack;

public final class Debug {
	private Debug() {}
	
	public static void printStack(Stack<?> stack) {
		System.out.println("____ Stack ____");
		Enumeration<?> elements = stack.elements();
		int i = 0;
		while (elements.hasMoreElements()) {
			System.out.print(++i + ": ");
			System.out.println(elements.nextElement());
		}
	}
}
