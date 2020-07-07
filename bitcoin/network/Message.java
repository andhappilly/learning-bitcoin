package bitcoin.network;

import static bitcoin.util.Functions.areEqual;
import static bitcoin.util.Functions.checkNull;
import static bitcoin.util.Functions.checkNullOrEmpty;
import static bitcoin.util.Functions.isNull;

import java.util.HashMap;
import java.util.Map;

import bitcoin.network.message.VerAck;
import bitcoin.network.message.Version;
import bitcoin.util.BytesEncodeable;
import bitcoin.util.InputSource;

public abstract class Message extends BytesEncodeable {
	private static final Map<String, String> TYPES = new HashMap<String, String>();
	
	private String type;
	
	protected Message(String type) {
		checkNullOrEmpty(type);
		
		checkRegistered(getClass(), type);
		this.type = type;
	}
	
	public final String getType() {
		return type;
	}
	
	public static Message parse(String type, InputSource source) {
		if (Version.TYPE.equals(type)) {
			return Version.parse(source);
		}
		
		if (VerAck.TYPE.equals(type)) {
			return VerAck.parse(source);
		}
		
		throw new UnsupportedOperationException("type not supported");
	}
	
	protected static synchronized void register(Class<? extends Message> clazz, String type) {
		checkNull(clazz);
		
		String clazzName = clazz.getName();
		
		String mappedClass = TYPES.get(type);		
		if (isNull(mappedClass)) {
			TYPES.put(type, clazzName);
			return;
		}
		
		if (!areEqual(mappedClass, clazzName)) {
			throw new IllegalStateException("Two message classes cannot be mapped to the same type.");
		}
	}
	
	private static void checkRegistered(Class<? extends Message> clazz, String type) {
		String mappedClass = TYPES.get(type);		
		String clazzName = clazz.getName();
		
		if (!areEqual(mappedClass, clazzName)) {
			throw new IllegalStateException("Message class is not registered.");
		}
	}
}
