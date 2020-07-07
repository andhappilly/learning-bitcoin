package bitcoin.util;

import static bitcoin.util.Bytes.newBytes;
import static bitcoin.util.Functions.checkNull;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public final class InputSource {
	private transient InputStream stream;
	private transient byte[] bytes;
	private transient Reader reader;
	private transient int bIndex;
	
	private InputSource(InputStream source) {
		checkNull(source);
		
		this.stream = source;
	}
	
	private InputSource(byte[] source) {
		checkNull(source);
		
		this.bytes = source;
	}
	
	private InputSource(Reader source) {
		checkNull(source);
		
		this.reader = source;
	}
	
	public static InputSource wrap(InputStream source) {
		return new InputSource(source);
	}
	
	public static InputSource wrap(Reader source) {
		return new InputSource(source);
	}
	
	public static InputSource wrap(byte[] source) {
		return new InputSource(source);
	}
	
	public char readNextChar() {
		return readNextChar(true);
	}
	
	public char readNextChar(boolean expected) {
		openAsReader();
		
		int c = -1;
		try {
			c = reader.read();
		} catch (IOException e) {
			throw new RuntimeException("Error reading from stream/reader.", e);
		}
		
		if (c == -1) {
			if (expected) {
				throw new IllegalStateException("Next character not available in stream/reader.");
			}
		}
		
		return (char)c;
	}
	
	public char[] readNextChars(int nChars) {
		return readNextChars(nChars, true);
	}
	
	public char[] readNextChars(int nChars, boolean exactExpected) {
		if (nChars < 1) {
			throw new IllegalArgumentException("Characters read cannot be less than 1.");
		}
		
		openAsReader();
		
		char[] buffer = new char[nChars];
		int readCount = -1;
		try {
			readCount = reader.read(buffer);
		} catch (IOException e) {
			throw new RuntimeException("Error reading from stream/reader.", e);
		}
		
		if (readCount < nChars) {
			if (exactExpected) {				
				throw new IllegalStateException("Expected characters not available in stream/reader.");
			}
			
			if (readCount == -1) {
				return null;
			}
			
			char[] result = new char[readCount];
			System.arraycopy(buffer, 0, result, 0, readCount);
			
			return result;
		}
		
		return buffer;
	}
	
	public byte readNextByte() {
		return readNextByte(true);
	}
	
	public byte readNextByte(boolean expected) {
		if (bytes != null) {
			return readNextByteFromByteArray(expected);
		}
		
		if (stream != null) {
			return readNextByteFromStream(expected);
		}
		
		// We can implement a stream from reader, but for now
		// we will just throw IllegalState exception ...
		throw new IllegalStateException("Stream not available.");
	}
	
	public byte[] readNextBytes(int nBytes) {
		if (bytes != null) {
			return readNextBytesFromByteArray(nBytes, true);
		}
		
		if (stream != null) {
			return readNextBytesFromStream(nBytes, true);
		}
		
		// We can implement a stream from reader, but for now
		// we will just throw IllegalState exception ...
		throw new IllegalStateException("Stream not available.");
	}
	
	private byte readNextByteFromStream(boolean expected) {
		int c = -1;
		try {
			c = stream.read();
		} catch (IOException e) {
			throw new RuntimeException("Error reading from stream/reader.", e);
		}
		
		if (c == -1) {
			if (expected) {
				throw new IllegalStateException("Next character not available in stream/reader.");
			}
		}
		
		return (byte)c;
	}
	
	private byte readNextByteFromByteArray(boolean expected) {
		if (bIndex >= bytes.length) {
			if (expected) {
				throw new IllegalStateException("Expected bytes not available in stream.");
			}
			
			return -1;
		}
		
		return bytes[bIndex++];
	}
	
	private byte[] readNextBytesFromStream(int nBytes, boolean exactExpected) {		
		if (nBytes < 1) {
			throw new IllegalArgumentException("bytes read cannot be less than 1.");
		}
		
		byte[] buffer = new byte[nBytes];
		int readCount = -1;
		try {
			readCount = stream.read(buffer);
		} catch (IOException e) {
			throw new RuntimeException("Error reading from stream/reader.", e);
		}
		
		if (readCount < nBytes) {
			if (exactExpected) {
				throw new IllegalStateException("Expected bytes not available in stream.");
			}
			
			if (readCount == -1) {
				return null;
			}
			
			return newBytes(buffer, 0, readCount);
		}
		
		return buffer;		
	}
	
	private byte[] readNextBytesFromByteArray(int nBytes, boolean expected) {		
		if (nBytes < 1) {
			throw new IllegalArgumentException("Characters read cannot be less than 1.");
		}
		
		if (bIndex >= bytes.length) {
			if (expected) {
				throw new IllegalStateException("Expected bytes not available in stream.");
			}
			
			return new byte[0];
		}
		
		int upto = bIndex + nBytes;
		if (upto > bytes.length) {
			if (expected) {
				throw new IllegalStateException("Expected bytes not available in stream.");
			}
			
			upto = bytes.length;
		}
		
		byte[] sub = newBytes(bytes, bIndex, upto);
		bIndex = upto;
		
		return sub;
	}
	
	private void openAsReader() {
		if (reader == null) {
			if (bytes == null) {
				reader = new BufferedReader(new InputStreamReader(stream));
			} else {
				reader = new BufferedReader(new InputStreamReader(
						new ByteArrayInputStream(bytes)));
			}
		}
	}
}
