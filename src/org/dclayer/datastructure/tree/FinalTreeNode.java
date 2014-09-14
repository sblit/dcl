package org.dclayer.datastructure.tree;

/**
 * a tree node with no child nodes
 * @param <T> the type of the value
 */
public class FinalTreeNode<T> extends TreeNode<T> {
	
	/**
	 * the key for the value
	 */
	private long key;
	/**
	 * the value
	 */
	private T value;

	@Override
	public T get(long key) {
		return (this.key == key) ? value : null;
	}

	@Override
	public void put(long key, T value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public boolean isFinal() {
		return true;
	}

	@Override
	public long getFinalKey() {
		return key;
	}

	@Override
	public T getFinalValue() {
		return value;
	}

	@Override
	public T remove(long key) {
		return null;
	}

	@Override
	public TreeNode<T>[] getChildren() {
		return null;
	}

	@Override
	public String toString() {
		return String.format("FinalTreeNode(key=%d, value=%s)", key, value);
	}
	
}
