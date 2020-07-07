package bitcoin.util;

import static bitcoin.util.Bytes.combine;
import static bitcoin.util.Bytes.newBytes;
import static bitcoin.util.Bytes.reverse;
import static bitcoin.util.Crypto.hash256BigInt;
import static bitcoin.util.Functions.checkNull;
import static bitcoin.util.Functions.isNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class MerkleTree {		
	private List<List<BigInteger>> nodes;
	
	private int currentLevel;
	private int currentIndex;
	
	public MerkleTree(int total) {
		if (total < 0) {
			throw new IllegalArgumentException("Total number of leaf nodes cannot be less than 0.");
		}		
		
		this.nodes = new ArrayList<List<BigInteger>>();
		
		int size = total;
		while (size > 1) {			
			List<BigInteger> sameLeveleNodes = new ArrayList<BigInteger>(size);
			for (int i = 0; i < size; ++i) {
				sameLeveleNodes.add(null);
			}
			
			nodes.add(sameLeveleNodes);
			
			size = ensureEven(size);
			size = size / 2;
		}
		
		List<BigInteger> rootLevel = new ArrayList<BigInteger>();
		rootLevel.add(null);
		
		nodes.add(rootLevel);
	}
	
	public void populate(List<Boolean> flags, List<BigInteger> hashes) {
		checkNull(flags, hashes);
		
		int fIndex = 0;
		int hIndex = 0;
		while (isNull(getRoot())) {
			if (isLeaf()) {
				// For leaf nodes, hashes are always provided ...
				setCurrentNode(hashes.get(hIndex));				
				moveUp();
				++fIndex;
				++hIndex;
			} else {
				BigInteger left = getDownLeftNode();
				if (isNull(left)) {
					// check if hash is provided ...
					if (Boolean.TRUE.equals(flags.get(fIndex))) {
						// If hash provided. set the hash and move up ...
						setCurrentNode(hashes.get(hIndex));	
						moveUp();
						++hIndex;
					} else {
						moveDownLeft();
					}
					++fIndex;
				} else if (existsDownRightNode()) {
					BigInteger right = getDownRightNode();
					if (isNull(right)) {
						moveDownRight();
					} else {
						setCurrentNode(parentOf(left, right));
						moveUp();
					}
				} else {
					setCurrentNode(parentOf(left, left));
					moveUp();
				}
			}
		}
		
		if (hIndex != hashes.size()) {
			throw new IllegalStateException("Not all hashes consumed.");
		}
		
		for (int i = fIndex; i < flags.size(); ++i) {
			if (Boolean.TRUE.equals(flags.get(fIndex))) {
				throw new IllegalStateException("Not all flags consumed.");
			}
		}
	}
	
	public BigInteger getRoot() {
		return nodes.get(0).get(0);
	}
	
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		for (int i = nodes.size() - 1; i >= 0 ; --i) {
			List<BigInteger> sameLeveleNodes = nodes.get(i);
			buffer.append("[");
			for (int j = 0; j < sameLeveleNodes.size(); ++j) {
				BigInteger value = sameLeveleNodes.get(j);
				buffer.append(isNull(value) ? "None" : value.toString(16)).append(", ");
			}
			
			buffer.setLength(buffer.length() - 2);
			buffer.append("]\n");
		}
		
		return buffer.toString();
	}
	
	public static BigInteger parentOf(BigInteger left, BigInteger right) {
		checkNull(left, right);		
		
		byte[] l = left.toByteArray();	
		byte[] r = right.toByteArray();	
		
		// sha-256 and hash-256 are 32 bytes, so we need to remove the extra byte
		// if required ...		
		int indexL = l.length > 32 ? (l.length - 32) : 0;
		int indexR = r.length > 32 ? (r.length - 32) : 0;
		
		return hash256BigInt(combine(l, indexL, l.length, r, indexR, r.length));
	}
	
	public static List<BigInteger> parentsOf(List<BigInteger> children) {
		checkNull(children);
		
		if (children.isEmpty()) {
			return Collections.emptyList();
		}
		
		int actualSize = children.size();		
		int adjustedSize = ensureEven(actualSize);
		boolean odd = actualSize != adjustedSize;
		int lastPair = adjustedSize - 2;
		
		List<BigInteger> parents = new ArrayList<BigInteger>(adjustedSize / 2);
		for (int i = 0; i < adjustedSize; i = i + 2) {
			BigInteger left, right;
			if (odd && (i == lastPair)) {
				left = children.get(i);
				right = left;				
			} else {
				left = children.get(i);
				right = children.get(i + 1);
			}
			
			BigInteger parent = parentOf(left, right);
			parents.add(parent);
		}
		
		return parents;
	}
	
	public static BigInteger rootOf(List<BigInteger> children) {
		checkNull(children);
		
		if (children.isEmpty()) {
			return null;
		}
		
		while(!children.isEmpty() && children.size() > 2) {
			children = parentsOf(children);
		}
		
		if (children.size() == 1) {
			return children.get(0);
		}
		
		return parentOf(children.get(0), children.get(1));
	}
	
	public static BigInteger reverseOf(BigInteger hash) {
		checkNull(hash);
		
		byte[] b = hash.toByteArray();
		
		// sha-256 and hash-256 are 32 bytes, so we need to remove the extra byte
		// if required ...		
		int index = b.length > 32 ? (b.length - 32) : 0;
		if (b.length > 32) {
			b = newBytes(b, 1, b.length);
		}
		
		reverse(b);
		
		return new BigInteger(1, b);
	}
	
	public static List<BigInteger> reversesOf(List<BigInteger> hashes) {
		checkNull(hashes);
		
		List<BigInteger> reverses = new ArrayList<BigInteger>(hashes.size());
		for (BigInteger hash: hashes) {
			reverses.add(reverseOf(hash));
		}
		
		return reverses;
	}
	
	private boolean isLeaf() {
		return currentLevel == nodes.size();
	}
	
	private boolean existsDownRightNode() {
		return nodes.get(currentLevel + 1).size() > currentIndex * 2 + 1;
	}
	
	private void moveUp() {
		currentLevel = currentLevel - 1;
		currentIndex = currentIndex / 2;		
	}
	
	private void moveDownLeft() {
		currentLevel = currentLevel + 1;
		currentIndex = currentIndex * 2;		
	}
	
	private void moveDownRight() {
		currentLevel = currentLevel + 1;
		currentIndex = currentIndex * 2 + 1;		
	}
	
	private void setCurrentNode(BigInteger v) {
		nodes.get(currentLevel).set(currentIndex, v);
	}
	
	private BigInteger getCurrentNode() {
		return nodes.get(currentLevel).get(currentIndex);
	}
	
	private BigInteger getDownLeftNode() {
		return nodes.get(currentLevel + 1).get(currentIndex * 2);
	}
	
	private BigInteger getDownRightNode() {
		return nodes.get(currentLevel + 1).get(currentIndex * 2 + 1);
	}
	
	private static int ensureEven(int num) {
		boolean odd = (num & 1) == 1;
		return odd ? num + 1 : num;
	}
	
	public static void main(String[] args) {
		BigInteger left = new BigInteger("c117ea8ec828342f4dfb0ad6bd140e03a50720ece40169ee38bdc15d9eb64cf5", 16);
		BigInteger right = new BigInteger("c131474164b412e3406696da1ee20ab0fc9bf41c8f05fa8ceea7a08d672d7cc5", 16);
		BigInteger parent = parentOf(left, right);  
		System.out.println(parent.toString(16));
		System.out.println();
		
		List<BigInteger> children = Arrays.asList(
				new BigInteger("c117ea8ec828342f4dfb0ad6bd140e03a50720ece40169ee38bdc15d9eb64cf5", 16),
				new BigInteger("c131474164b412e3406696da1ee20ab0fc9bf41c8f05fa8ceea7a08d672d7cc5", 16),
				new BigInteger("f391da6ecfeed1814efae39e7fcb3838ae0b02c02ae7d0a5848a66947c0727b0", 16),
				new BigInteger("3d238a92a94532b946c90e19c49351c763696cff3db400485b813aecb8a13181", 16),
				new BigInteger("10092f2633be5f3ce349bf9ddbde36caa3dd10dfa0ec8106bce23acbff637dae", 16)
		);
		
		List<BigInteger> parents = parentsOf(children);
		for (BigInteger p : parents) {
			System.out.println(p.toString(16));
		}
		System.out.println();
		
		children = Arrays.asList(
				new BigInteger("c117ea8ec828342f4dfb0ad6bd140e03a50720ece40169ee38bdc15d9eb64cf5", 16),
				new BigInteger("c131474164b412e3406696da1ee20ab0fc9bf41c8f05fa8ceea7a08d672d7cc5", 16),
				new BigInteger("f391da6ecfeed1814efae39e7fcb3838ae0b02c02ae7d0a5848a66947c0727b0", 16),
				new BigInteger("3d238a92a94532b946c90e19c49351c763696cff3db400485b813aecb8a13181", 16),
				new BigInteger("10092f2633be5f3ce349bf9ddbde36caa3dd10dfa0ec8106bce23acbff637dae", 16),
				new BigInteger("7d37b3d54fa6a64869084bfd2e831309118b9e833610e6228adacdbd1b4ba161", 16),
				new BigInteger("8118a77e542892fe15ae3fc771a4abfd2f5d5d5997544c3487ac36b5c85170fc", 16),
				new BigInteger("dff6879848c2c9b62fe652720b8df5272093acfaa45a43cdb3696fe2466a3877", 16),
				new BigInteger("b825c0745f46ac58f7d3759e6dc535a1fec7820377f24d4c2c6ad2cc55c0cb59", 16),
				new BigInteger("95513952a04bd8992721e9b7e2937f1c04ba31e0469fbe615a78197f68f52b7c", 16),
				new BigInteger("2e6d722e5e4dbdf2447ddecc9f7dabb8e299bae921c99ad5b0184cd9eb8e5908", 16),
				new BigInteger("b13a750047bc0bdceb2473e5fe488c2596d7a7124b4e716fdd29b046ef99bbf0", 16)
		);
		
		BigInteger root = rootOf(children);
		System.out.println(root.toString(16));
		System.out.println();
		
		MerkleTree tree = new MerkleTree(16);
		System.out.println(tree);
	}
}
