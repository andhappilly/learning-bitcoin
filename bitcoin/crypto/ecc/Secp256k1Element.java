package bitcoin.crypto.ecc;

import java.math.BigInteger;

import bitcoin.math.field.finite.Element;

import static bitcoin.crypto.ecc.Secp256k1.P;
import static bitcoin.util.BigInt.from;

public final class Secp256k1Element extends Element {
	private static final BigInteger P_PLUS_1_BY_4 = 
			P.add(BigInteger.ONE).divide(from(4));
	
	public Secp256k1Element(BigInteger number) {
		super(number, P);
	}
	
	public Secp256k1Element sqrt() {
		return new Secp256k1Element(power(P_PLUS_1_BY_4).value());
	}
}
