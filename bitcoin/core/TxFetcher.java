package bitcoin.core;

import static bitcoin.util.Functions.isNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import bitcoin.util.InputSource;

public final class TxFetcher {
	private static final int MAX_ENTRIES = 100;
	
	private static final String TESTNET_URL = "http://testnet.programmingbitcoin.com/tx/";
	private static final String MAINNET_URL = "http://mainnet.programmingbitcoin.com/tx/";
	
	private static final Map<BigInteger, Transaction> CACHE = 
			Collections.synchronizedMap(
				new LinkedHashMap<BigInteger, Transaction>(100) {
					private static final long serialVersionUID = 1L;
	
					protected boolean removeEldestEntry(Map.Entry<BigInteger, Transaction> eldest) {
				        return size() > MAX_ENTRIES;
				     }
				}
			);
	private TxFetcher() {}
	
	public static Transaction fetch(BigInteger txId) {
		return fetch(txId, false, false);
	}
	
	public static Transaction fetch(BigInteger txId, boolean testnet, boolean fresh) {
		Transaction tx = null;
		if (!fresh) {
			tx = CACHE.get(txId);
		}
		
		if (fresh || isNull(tx)) {
			String txResource = buildTxURL(txId, testnet);
			tx = fetchTx(txResource);
			
			CACHE.put(txId, tx);
		}
		
		return tx;
	}
	
	private static String buildTxURL(BigInteger txId, boolean testnet) {
		String txIdHex = txId.toString(16);
		
		// Transaction ids are expected to be 32 bytes or 64 characters, hence
		// if it is less than that we need to compensate for that with prefixed 0s.		
		int diff = 64 - txIdHex.length(); 
		
		if (diff < 0) {
			throw new IllegalArgumentException("Invalid transaction id.");
		}
		
		StringBuilder buffer = 
				new StringBuilder(TESTNET_URL.length() + diff + txIdHex.length() + 4);
		
		buffer.append(testnet ? TESTNET_URL : MAINNET_URL);
		
		if (diff > 0) {			 
			for(int i = 0; i < diff; ++i) {
				buffer.append('0');
			}
		} 		
		
		buffer.append(txIdHex).append(".hex");
		
		return buffer.toString();
	}
	
	private static Transaction fetchTx(String txResource) {
		URL url;
		try {
			url = new URL(txResource);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
		
		Transaction tx;
		try(BufferedInputStream stream = new BufferedInputStream(url.openStream())) {
			InputSource source = InputSource.wrap(stream);
			tx = Transaction.parse(source);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		return tx;
	}
}
