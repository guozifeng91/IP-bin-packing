package layoutIP;

import java.util.ArrayList;

/**
 * A simple implementation of binary tree
 * 
 * @author guoguo
 *
 * @param <T>
 */
public class BinaryTreeNode<T> {
	T object;
	BinaryTreeNode<T> father;
	BinaryTreeNode<T> leftChild, rightChild;

	public BinaryTreeNode() {
		this(null);
	}

	public BinaryTreeNode(T t) {
		object = t;
	}

	public T getObject() {
		return object;
	}

	public void attachToLeft(BinaryTreeNode<T> father) {
		father.detachLeftChild();
		father.leftChild = this;
		this.father = father;
	}

	public void attachToRight(BinaryTreeNode<T> father) {
		father.detachRightChild();
		father.rightChild = this;
		this.father = father;
	}

	public BinaryTreeNode<T> detachFromFather() {
		BinaryTreeNode<T> oldFather = father;
		if (father != null) {
			if (father.leftChild == this)
				father.leftChild = null;
			if (father.rightChild == this)
				father.rightChild = null;
			father = null;
		}
		return oldFather;
	}

	public BinaryTreeNode<T> detachLeftChild() {
		BinaryTreeNode<T> old = leftChild;
		if (leftChild != null) {
			leftChild.detachFromFather();
		}
		return old;
	}

	public BinaryTreeNode<T> detachRightChild() {
		BinaryTreeNode<T> old = rightChild;
		if (rightChild != null) {
			rightChild.detachFromFather();
		}
		return old;
	}

	public ArrayList<BinaryTreeNode<T>> getChildren(boolean addLeftChild, boolean addRightChild) {
		ArrayList<BinaryTreeNode<T>> allNodes = new ArrayList<BinaryTreeNode<T>>();
		addToList(allNodes, this, addLeftChild, addRightChild);
		return allNodes;
	}

	private void addToList(ArrayList<BinaryTreeNode<T>> buf, BinaryTreeNode<T> n, boolean l, boolean r) {
		if (n == null)
			return;

		buf.add(n);
		if (l) {
			addToList(buf, n.leftChild, l, r);
		}
		if (r) {
			addToList(buf, n.rightChild, l, r);
		}
	}

	public BinaryTreeNode<T> getFather() {
		return father;
	}

	public BinaryTreeNode<T> getChildLeft() {
		return leftChild;
	}

	public BinaryTreeNode<T> getChildRight() {
		return rightChild;
	}

	public BinaryTreeNode<T> getLeafLeft() {
		BinaryTreeNode<T> leaf = this;
		while (leaf.leftChild != null) {
			leaf = leaf.leftChild;
		}
		return leaf;
	}

	public BinaryTreeNode<T> getLeafRight() {
		BinaryTreeNode<T> leaf = this;
		while (leaf.rightChild != null) {
			leaf = leaf.rightChild;
		}
		return leaf;
	}
}
