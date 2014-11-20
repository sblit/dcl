package org.dclayer.datastructure.tree;

import org.dclayer.net.Data;

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
	 * the offset in bytes from the key's most significant byte (at index 0) to the key's least significant byte (at index (key.length - 1)).
	 * the byte at byteOffset specifies the index for the children array
	 */
	private final int byteOffset;
	
	/**
	 * creates a new {@link ParentTreeNode} using the specified bitOffset value
	 * @param byteOffset the offset in bytes from the key's most significant byte (at index 0) to the key's least significant byte
	 */
	public ParentTreeNode(int byteOffset) {
		this.byteOffset = byteOffset;
	}

	@Override
	public T get(Data key) {
		final int i = 0xFF & key.getByte(byteOffset);
		TreeNode<T> child = children[i];
		return (child == null) ? null : child.get(key);
	}
	
	@Override
	public T getClosest(Data key, int direction) {
		final int i = 0xFF & key.getByte(byteOffset);
		TreeNode<T> child = children[i];
		if(child == null) {
			if(direction > 0) {
				for(int j = children.length-1; j >= 0; j--) {
					if(children[j] != null) return children[j].getClosest(key, direction);
				}
				return null;
			} else if(direction < 0) {
				for(int j = 0; j < children.length; j++) {
					if(children[j] != null) return children[j].getClosest(key, direction);
				}
				return null;
			} else {
				int min = 0;
				TreeNode<T> closest = null;
				for(int j = i+1; j < children.length; j++) {
					if(children[j] != null) {
						closest = children[j];
						min = Math.max(0, i-(j-i));
						break;
					}
				}
				for(int j = i-1; j >= min; j--) {
					if(children[j] != null) return children[j].getClosest(key, 1);
				}
				return closest == null ? null : closest.getClosest(key, -1);
			}
		} else {
			return child.getClosest(key);
		}
	}

	@Override
	public T remove(Data key) {
		final int i = 0xFF & key.getByte(byteOffset);
		TreeNode<T> child = children[i];
		if(child == null) {
			return null;
		}
		if(child.isFinal() && child.getFinalKey().equals(key)) {
			children[i] = null;
			return child.getFinalValue();
		} else {
			return child.remove(key);
		}
	}

	@Override
	public void put(Data key, T value) {
		final int i = 0xFF & key.getByte(byteOffset);
		TreeNode<T> child = children[i];
		if(child == null) {
			child = children[i] = new FinalTreeNode<T>();
			child.put(key, value);
		} else if(child.isFinal() && !child.getFinalKey().equals(key)) {
			TreeNode<T> old = child;
			child = children[i] = new ParentTreeNode<T>(byteOffset + 1);
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
	public Data getFinalKey() {
		return null;
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
		return String.format("ParentTreeNode(byteOffset=%d)", byteOffset);
	}
	
}
