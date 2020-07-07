package misc;
import bitcoin.lang.op.Op2;
import bitcoin.lang.op.Op2Dup;
import bitcoin.lang.op.Op6;
import bitcoin.lang.op.OpAdd;
import bitcoin.lang.op.OpCheckSig;
import bitcoin.lang.op.OpCode;
import bitcoin.lang.op.OpDup;
import bitcoin.lang.op.OpEqual;
import bitcoin.lang.op.OpEqualVerify;
import bitcoin.lang.op.OpHash160;
import bitcoin.lang.op.OpHash256;
import bitcoin.lang.op.OpMul;
import bitcoin.lang.op.OpNot;
import bitcoin.lang.op.OpSha1;
import bitcoin.lang.op.OpSwap;
import bitcoin.lang.op.OpVerify;

public final class ExercisesUtil {
	private ExercisesUtil() {}
	
	public static void loadOpCodes() {
		// Make sure all opcodes are loaded before running the exercise ...
		OpCode[] opcodes = new OpCode[] {
				Op2.INSTANCE, Op6.INSTANCE, OpAdd.INSTANCE,
				OpCheckSig.INSTANCE, OpDup.INSTANCE, OpEqual.INSTANCE,
				OpEqualVerify.INSTANCE, OpHash160.INSTANCE, OpHash256.INSTANCE,
				OpMul.INSTANCE, OpSwap.INSTANCE, Op2Dup.INSTANCE,
				OpNot.INSTANCE, OpSha1.INSTANCE, OpVerify.INSTANCE
		};
		
		System.out.println("_____ Loaded OPCODES _____");
		for (OpCode o: opcodes) {
			System.out.print(o.getName() + " ");
		}
		System.out.println();
		System.out.println();
	}
}
