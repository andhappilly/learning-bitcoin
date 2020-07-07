package bitcoin.lang;

import bitcoin.util.BytesEncodeable;

public abstract class Command extends BytesEncodeable {
	public abstract boolean isExecutable();
}
