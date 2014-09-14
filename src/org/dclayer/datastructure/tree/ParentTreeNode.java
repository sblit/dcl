package org.dclayer.datastructure.tree;

/**
 * a tree node with child nodes
 * @param <T> the type of the value
 */
public class ParentTreeNode<T> extends TreeNode<T> {
	
	/**
	 * array containing this node's children
	 */
	private final TreeNode<T>[] children = new TreeNode[256];
	/**
	 * the offset in bits from the key's least significant bit (0) to the key's most significant bit (63).
	 * the 8 bits from bitOffset to bitOffset+7 specify the index for the children array
	 */
	private final int bitOffset;
	
	/**
	 * creates a new {@link ParentTreeNode} using the specified bitOffset value
	 * @param bitOffset the offset in bits from the key's least significant bit (0) to the key's most significant bit (63). the 8 bits from bitOffset to bitOffset+7 specify the index for the children array
	 */
	public ParentTreeNode(int bitOffset) {
		this.bitOffset = bitOffset;
	}

	@Override
	public T get(long key) {
		final int i = (int)((key >> bitOffset) & 0xFF);
		TreeNode<T> child = children[i];
		return (child == null) ? null : child.get(key);
	}

	@Override
	public T remove(long key) {
		final int i = (int)((key >> bitOffset) & 0xFF);
		TreeNode<T> child = children[i];
		if(child == null) {
			return null;
		}
		if(child.isFinal() && child.getFinalKey() == key) {
			children[i] = null;
			return child.getFinalValue();
		} else {
			return child.remove(key);
		}
	}

	@Override
	public void put(long key, T value) {
		final int i = (int)((key >> bitOffset) & 0xFF);
		TreeNode<T> child = children[i];
		if(child == null) {
			child = children[i] = new FinalTreeNode<T>();
			child.put(key, value);
		} else if(child.isFinal() && child.getFinalKey() != key) {
			TreeNode<T> old = child;
			child = children[i] = new ParentTreeNode<T>(bitOffset + 8);
			child.put(old.getFinalKey(), old.getFinalValue());
			child.put(key, value);
		} else {
			child.put(key, value);
		}
	}
	
	@Override
	public boolean isFinal() {
		return false;
	}

	@Override
	public long getFinalKey() {
		return 0;
	}

	@Override
	public T getFinalValue() {
		return null;
	}

	@Override
	public TreeNode<T>[] getChildren() {
		return children;
	}

	@Override
	public String toString() {
		return String.format("ParentTreeNode(bitOffset=%d)", bitOffset);
	}
	
}
