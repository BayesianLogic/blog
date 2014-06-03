/*
 * Copyright (c) 2006, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.  
 *
 * * Neither the name of the University of California, Berkeley nor
 *   the names of its contributors may be used to endorse or promote
 *   products derived from this software without specific prior 
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package blog.common;

import java.util.*;

/**
 * An implementation of the IndexedSet interface that keeps elements in sorted
 * order. The order can be the natural ordering of the elements (if they
 * implement the Comparable interface), or it can be specified by passing a
 * Comparator object to the constructor.
 * 
 * <p>
 * This implementation uses a red-black tree, as described in Chapter 13 of
 * Cormen, Leiserson, Rivest and Stein, "Introduction to Algorithms", 2nd ed.
 * Each node in the tree also stores the size of the subtree rooted at it; this
 * allows us to find the element at any given index in the ordering in O(lg n)
 * time.
 */
public class IndexedTreeSet extends AbstractSet implements IndexedSortedSet,
		Cloneable {
	/**
	 * Creates an empty IndexedTreeSet that uses the natural ordering on its
	 * elements. The elements must implement the Comparable interface.
	 */
	public IndexedTreeSet() {
	}

	/**
	 * Creates an empty IndexedTreeSet that uses the given comparator.
	 */
	public IndexedTreeSet(Comparator comparator) {
		this.comparator = comparator;
	}

	/**
	 * Creates an IndexedTreeSet that contains the same elements as the given
	 * collection and uses the natural ordering on the elements. The elements must
	 * implement the Comparable interface.
	 */
	public IndexedTreeSet(Collection c) {
		addAll(c);
	}

	/**
	 * Creates an IndexedTreeSet that contains the same elements as the given
	 * SortedSet and uses the same comparator.
	 */
	public IndexedTreeSet(SortedSet s) {
		comparator = s.comparator();
		addAll(s);
	}

	public boolean contains(Object o) {
		return (getNode(o) != null);
	}

	public int size() {
		return root.subtreeSize;
	}

	public Iterator iterator() {
		return new TreeIterator(null, null);
	}

	public boolean add(Object o) {
		Node parent = null;
		Node newNode = null;

		if (root == NULL_CHILD) {
			newNode = new Node(null, o);
			root = newNode;
		} else {
			parent = root;
			while (true) {
				int comp = compare(o, parent.key);
				if (comp == 0) {
					return false; // o is already in the tree
				}
				if (comp < 0) {
					if (parent.left == NULL_CHILD) {
						newNode = new Node(parent, o);
						parent.left = newNode;
						break;
					}
					parent = parent.left;
				} else {
					if (parent.right == NULL_CHILD) {
						newNode = new Node(parent, o);
						parent.right = newNode;
						break;
					}
					parent = parent.right;
				}
			}
		}

		adjustSubtreeSizes(newNode, 1);
		adjustForInsertion(newNode);
		return true;
	}

	public boolean remove(Object o) {
		Node node = getNode(o);
		if (node == null) {
			return false;
		}

		deleteNode(node);
		return true;
	}

	public void clear() {
		root = null;
	}

	public Comparator comparator() {
		return comparator;
	}

	public Object first() {
		if (root == NULL_CHILD) {
			throw new NoSuchElementException("Can't get first element of empty set.");
		}

		Node firstNode = firstNode();
		return firstNode.key;
	}

	public Object last() {
		if (root == NULL_CHILD) {
			throw new NoSuchElementException("Can't get last element of empty set.");
		}

		Node lastNode = lastNode();
		return lastNode.key;
	}

	public SortedSet headSet(Object toElement) {
		return new SubSet(null, toElement);
	}

	public SortedSet tailSet(Object fromElement) {
		return new SubSet(fromElement, null);
	}

	public SortedSet subSet(Object fromElement, Object toElement) {
		return new SubSet(fromElement, toElement);
	}

	public Object get(int index) {
		if ((index < 0) || (index >= size())) {
			throw new IndexOutOfBoundsException("Can't get element " + index
					+ " of IndexedTreeSet of size " + size());
		}

		Node x = root;
		int indexInSubtree = index;
		while (x != NULL_CHILD) {
			int leftSubtreeSize = x.left.subtreeSize;
			if (indexInSubtree == leftSubtreeSize) {
				break;
			}
			if (indexInSubtree < leftSubtreeSize) {
				x = x.left;
			} else {
				indexInSubtree -= (leftSubtreeSize + 1);
				x = x.right;
			}
		}

		return x.key;
	}

	public int indexOf(Object o) {
		if (o == null) {
			return -1;
		}

		Node x = getNode(o);
		if (x == null) {
			return -1;
		}
		return getNodeIndex(x);
	}

	public Object clone() {
		IndexedTreeSet clone = new IndexedTreeSet(comparator);
		clone.root = root.deepCopy();
		return clone;
	}

	public void print() {
		printTree(root, 0);
		System.out.println();
	}

	private Node getNode(Object o) {
		Node x = root;
		while (x != NULL_CHILD) {
			int comp = compare(o, x.key);
			if (comp == 0) {
				return x;
			}
			x = (comp < 0) ? x.left : x.right;
		}
		return null;
	}

	private int getNodeIndex(Node x) {
		int index = x.left.subtreeSize;
		while (x.parent != null) {
			if (x == x.parent.right) {
				index += (x.parent.left.subtreeSize + 1);
			}
			x = x.parent;
		}
		return index;
	}

	private Node getLeastNodeGreaterOrEqual(Object o) {
		Node leastSoFar = null;
		Node x = root;
		while (x != NULL_CHILD) {
			int comp = compare(o, x.key);
			if (comp == 0) {
				return x;
			}
			if (comp < 0) {
				// x is greater than or equal to o
				// It must be the least such node so far because when we
				// encounter such a node, we go down its left subtree, so
				// all nodes we visit subsequently are less than it.
				leastSoFar = x;
			}
			x = (comp < 0) ? x.left : x.right;
		}
		return leastSoFar;
	}

	private Node getGreatestNodeLessThan(Object o) {
		Node greatestSoFar = null;
		Node x = root;
		while (x != NULL_CHILD) {
			int comp = compare(o, x.key);
			if (comp > 0) {
				// x is less than o
				// It must be the greatest such node so far because when
				// we encounter such a node, we go down its right subtree,
				// so all nodes we visit subsequently are greater than it.
				greatestSoFar = x;
			}
			x = (comp <= 0) ? x.left : x.right;
		}
		return greatestSoFar;
	}

	private void adjustSubtreeSizes(Node x, int offset) {
		while (x != null) {
			x.subtreeSize += offset;
			x = x.parent;
		}
	}

	private void leftRotate(Node x) {
		/* Assumes x.right != NULL_CHILD */
		Node y = (x.right);

		x.right = y.left;
		if (y.left != NULL_CHILD) {
			y.left.parent = x;
		}

		y.parent = x.parent;
		if ((x.parent) == null) {
			root = y;
		} else {
			if (x == (x.parent.left)) {
				(x.parent.left) = y;
			} else {
				(x.parent.right) = y;
			}
		}

		y.left = x;
		x.parent = y;

		y.subtreeSize = x.subtreeSize;
		x.subtreeSize = (x.left.subtreeSize + x.right.subtreeSize + 1);
	}

	void rightRotate(Node x) {
		/* Assumes x.left != NULL_CHILD */
		Node y = (x.left);

		x.left = y.right;
		if (y.right != NULL_CHILD) {
			y.right.parent = x;
		}

		y.parent = x.parent;
		if ((x.parent) == null) {
			root = y;
		} else {
			if (x == x.parent.right) {
				(x.parent.right) = y;
			} else {
				(x.parent.left) = y;
			}
		}

		y.right = x;
		x.parent = y;

		y.subtreeSize = x.subtreeSize;
		x.subtreeSize = (x.left.subtreeSize + x.right.subtreeSize + 1);
	}

	private void adjustForInsertion(Node x) {
		x.color = RED;
		while ((x != (root)) && ((x.parent.color) == RED)) {
			if ((x.parent) == (x.parent.parent.left)) {
				Node y = x.parent.parent.right;
				if (y.color == RED) {
					x.parent.color = BLACK;
					y.color = BLACK;
					x.parent.parent.color = RED;
					x = x.parent.parent;
				} else {
					if (x == (x.parent.right)) {
						x = x.parent;
						leftRotate(x);
					}
					x.parent.color = BLACK;
					x.parent.parent.color = RED;
					rightRotate(x.parent.parent);
				}
			} else {
				/* same as if clause with right and left exchanged */
				Node y = ((x.parent).parent).left;
				if (y.color == RED) {
					x.parent.color = BLACK;
					y.color = BLACK;
					x.parent.parent.color = RED;
					x = x.parent.parent;
				} else {
					if (x == (x.parent.left)) {
						x = x.parent;
						rightRotate(x);
					}
					x.parent.color = BLACK;
					x.parent.parent.color = RED;
					leftRotate(x.parent.parent);
				}
			}
		}
		root.color = BLACK;
	}

	private void deleteNode(Node z) {
		// We will splice out a node y that has < 2 children. This is
		// z if z has < 2 children; otherwise it's z's successor
		// (which cannot have a left child in this case; see p. 262 of CLRS).
		Node y;
		if ((z.left == NULL_CHILD) || (z.right == NULL_CHILD)) {
			y = z;
		} else {
			y = succNode(z);
		}
		boolean mustRebalance = (y.color == BLACK);

		// Let x be y's non-null child, or NULL_CHILD if they're both null
		Node x = ((y.left != NULL_CHILD) ? y.left : y.right);

		// Attach x to y's former parent. x keeps its own children, if any.
		x.parent = y.parent;
		if (y.parent == null) {
			root = x;
		} else {
			if (y == y.parent.left) {
				y.parent.left = x;
			} else {
				y.parent.right = x;
			}
			adjustSubtreeSizes(y.parent, -1);
		}

		// If the node we're splicing out is not z, have it take z's
		// position in the tree.
		if (y != z) {
			putInPosition(y, z);
		}

		// Rebalance tree if necessary
		if (mustRebalance) {
			deleteFixup(x);
		}
	}

	private void deleteFixup(Node x) {
		Node w;

		while ((x != root) && ((x.color) == BLACK)) {
			if (x == x.parent.left) {
				w = x.parent.right;
				/* Note: according to p. 275 of CLR, w != null */
				if ((w.color) == RED) {
					w.color = BLACK;
					(x.parent).color = RED;
					leftRotate(x.parent);
					w = x.parent.right;
				}

				if ((w.left.color == BLACK) && (w.right.color == BLACK)) {
					w.color = RED;
					x = x.parent;
				} else {
					if (w.right.color == BLACK) {
						w.left.color = BLACK;
						w.color = RED;
						rightRotate(w);
						w = (x.parent).right;
					}

					w.color = (w.parent).color;
					(x.parent).color = BLACK;
					w.right.color = BLACK;
					leftRotate(x.parent);
					x = root;
				}
			} else {
				/* same as then clause with right and left exchanged */
				w = ((x.parent).left);
				/* Note: according to p. 275 of CLR, w != null */

				if ((w.color) == RED) {
					w.color = BLACK;
					(x.parent).color = RED;
					rightRotate(x.parent);
					w = (x.parent).left;
				}

				if ((w.right.color == BLACK) && (w.left.color == BLACK)) {
					w.color = RED;
					x = x.parent;
				} else {
					if (w.left.color == BLACK) {
						w.right.color = BLACK;
						w.color = RED;
						leftRotate(w);
						w = (x.parent).left;
					}

					w.color = (w.parent).color;
					(x.parent).color = BLACK;
					w.left.color = BLACK;
					rightRotate(x.parent);
					x = root;
				}
			}
		}
		x.color = BLACK;
	}

	private Node succNode(Node node) {
		Node currNode = node;
		Node parentNode;

		if (currNode.right != NULL_CHILD) {
			currNode = currNode.right;
			while (currNode.left != NULL_CHILD) {
				currNode = currNode.left;
			}
			return (currNode);
		}

		parentNode = currNode.parent;
		while (parentNode != null && currNode == parentNode.right) {
			currNode = parentNode;
			parentNode = currNode.parent;
		}
		return (parentNode);
	}

	private Node firstNode() {
		if (root == NULL_CHILD) {
			return null;
		}

		Node x = root;
		while (x.left != NULL_CHILD) {
			x = x.left;
		}
		return x;
	}

	private Node lastNode() {
		if (root == NULL_CHILD) {
			return null;
		}

		Node x = root;
		while (x.right != NULL_CHILD) {
			x = x.right;
		}
		return x;
	}

	/**
	 * Does something equivalent to copying the other node's key into the mover
	 * node, but in a way that won't invalidate iterators.
	 */
	private void putInPosition(Node mover, Node other) {
		mover.color = other.color;
		mover.subtreeSize = other.subtreeSize;
		mover.parent = other.parent;

		// attach to parent
		if (other == root) {
			root = mover;
		} else {
			if (other == mover.parent.left) {
				mover.parent.left = mover;
			} else {
				mover.parent.right = mover;
			}
		}

		// attach left child (even if it's NULL_CHILD)
		mover.left = other.left;
		mover.left.parent = mover;

		// attach right child (even if it's NULL_CHILD)
		mover.right = other.right;
		mover.right.parent = mover;
	}

	private void printTree(Node x, int indentLevel) {
		if (x == NULL_CHILD) {
			return;
		}

		printNode(x, indentLevel);
		if (x.left != NULL_CHILD) {
			printTree(x.left, indentLevel + 1);
		}
		if (x.right != NULL_CHILD) {
			printTree(x.right, indentLevel + 1);
		}
	}

	private void printNode(Node x, int indentLevel) {
		for (int i = 0; i < indentLevel; ++i) {
			System.out.print("\t");
		}

		if (x != NULL_CHILD) {
			if (x.parent != null) {
				if (x == x.parent.left) {
					System.out.print("L ");
				} else if (x == x.parent.right) {
					System.out.print("R ");
				} else {
					System.out.print("* ");
				}
			}
			System.out.println("(k,c,n) = (" + x.key + ", "
					+ ((x.color == RED) ? "R" : "B") + ", " + x.subtreeSize + ")");
		} else {
			System.out.println("node is null");
		}
	}

	private int compare(Object o1, Object o2) {
		if (comparator == null) {
			return ((Comparable) o1).compareTo(o2);
		}
		return comparator.compare(o1, o2);
	}

	private class SubSet extends AbstractSet implements SortedSet {
		SubSet(Object lower, Object upper) {
			this.lower = lower;
			this.upper = upper;
		}

		public int size() {
			int lowerIndex = 0;
			if (lower != null) {
				Node lowerNode = getLeastNodeGreaterOrEqual(lower);
				if (lowerNode == null) {
					return 0;
				}
				lowerIndex = getNodeIndex(lowerNode);
			}

			int upperIndex = IndexedTreeSet.this.size() - 1;
			if (upper != null) {
				Node upperNode = getGreatestNodeLessThan(upper);
				if (upperNode == null) {
					return 0;
				}
				upperIndex = getNodeIndex(upperNode);
			}

			return (upperIndex - lowerIndex + 1);
		}

		public boolean contains(Object o) {
			return (((lower == null) || (compare(o, lower) >= 0))
					&& ((upper == null) || (compare(o, upper) < 0)) && IndexedTreeSet.this
						.contains(o));
		}

		public Iterator iterator() {
			return new TreeIterator(lower, upper);
		}

		public Comparator comparator() {
			return comparator;
		}

		public Object first() {
			Node lowerNode = ((lower == null) ? firstNode()
					: getLeastNodeGreaterOrEqual(lower));
			if ((lowerNode == null)
					|| ((upper != null) && (compare(lowerNode.key, upper) >= 0))) {
				throw new NoSuchElementException(
						"Can't get first element of empty set.");
			}
			return lowerNode.key;
		}

		public Object last() {
			Node upperNode = ((upper == null) ? lastNode()
					: getGreatestNodeLessThan(upper));
			if ((upperNode == null)
					|| ((lower != null) && (compare(upperNode.key, lower) < 0))) {
				throw new NoSuchElementException("Can't get last element of empty set.");
			}
			return upperNode.key;
		}

		public SortedSet headSet(Object toElement) {
			Object newUpper = toElement;
			if ((upper != null) && (compare(toElement, upper) > 0)) {
				newUpper = upper;
			}
			return new SubSet(null, newUpper);
		}

		public SortedSet tailSet(Object fromElement) {
			Object newLower = fromElement;
			if ((lower != null) && (compare(fromElement, lower) < 0)) {
				newLower = lower;
			}
			return new SubSet(newLower, null);
		}

		public SortedSet subSet(Object fromElement, Object toElement) {
			Object newLower = fromElement;
			if ((lower != null) && (compare(fromElement, lower) < 0)) {
				newLower = lower;
			}

			Object newUpper = toElement;
			if ((upper != null) && (compare(toElement, upper) > 0)) {
				newUpper = upper;
			}

			return new SubSet(newLower, newUpper);
		}

		private Object lower;
		private Object upper;
	}

	private class TreeIterator implements Iterator {
		TreeIterator(Object lower, Object upper) {
			if (lower == null) {
				nextNode = firstNode();
			} else {
				nextNode = getLeastNodeGreaterOrEqual(lower);
			}

			this.upper = upper;
			if ((upper != null) && (compare(nextNode.key, upper) >= 0)) {
				nextNode = null;
			}
		}

		public boolean hasNext() {
			return (nextNode != null);
		}

		public Object next() {
			if (nextNode == null) {
				throw new NoSuchElementException();
			}
			Object toReturn = nextNode.key;
			lastNodeReturned = nextNode;

			nextNode = succNode(nextNode);
			if ((upper != null) && (compare(nextNode.key, upper) >= 0)) {
				nextNode = null;
			}

			return toReturn;
		}

		public void remove() {
			if (lastNodeReturned == null) {
				throw new IllegalStateException(
						"next has not been called since last call to remove.");
			}
			deleteNode(lastNodeReturned);
			lastNodeReturned = null;

			// nextNode is still the node with the next-largest key in
			// the set, so we don't need to change it
		}

		private Object upper;
		private Node nextNode;
		private Node lastNodeReturned = null;
	}

	private static class Node {
		Node(Node parent, Object key) {
			this.key = key;
			this.parent = parent;
		}

		Node deepCopy() {
			Node copy = new Node(parent, key);
			copy.color = color;

			if (left != NULL_CHILD) {
				copy.left = left.deepCopy();
				copy.left.parent = copy;
			}

			if (right != NULL_CHILD) {
				copy.right = right.deepCopy();
				copy.right.parent = copy;
			}

			copy.subtreeSize = subtreeSize;
			return copy;
		}

		Object key;
		boolean color = RED;
		Node parent;
		Node left = NULL_CHILD;
		Node right = NULL_CHILD;
		int subtreeSize = 0;
	}

	private static boolean BLACK = false;
	private static boolean RED = true;

	/**
	 * Node representing a null child of a leaf node. It is colored black and has
	 * a subtree size of zero. Its parent value has no permanent meaning; any leaf
	 * node can be set as its parent temporarily for convenience.
	 */
	private static Node NULL_CHILD;
	static {
		NULL_CHILD = new Node(null, null);
		NULL_CHILD.color = BLACK;
		NULL_CHILD.left = NULL_CHILD;
		NULL_CHILD.right = NULL_CHILD;
	}

	private Comparator comparator = null;
	private Node root = NULL_CHILD;

	/**
	 * Test program.
	 */
	public static void main(String[] args) {
		Util.initRandom(false);

		List numbers = new ArrayList();
		for (int i = 0; i < 100; ++i) {
			numbers.add(new Integer(i));
		}
		Util.shuffle(numbers);

		// Fill set with numbers
		IndexedTreeSet s = new IndexedTreeSet();
		for (Iterator iter = numbers.iterator(); iter.hasNext();) {
			s.add(iter.next());
		}
		s.print();
		System.out.println();

		// Test iteration
		for (Iterator iter = s.iterator(); iter.hasNext();) {
			System.out.print(iter.next() + " ");
		}
		System.out.println();

		// Test get method
		for (int i = 0; i < 100; ++i) {
			System.out.print(s.get(i) + " ");
		}
		System.out.println();

		// Test indexOf method
		for (Iterator iter = s.iterator(); iter.hasNext();) {
			System.out.print(s.indexOf(iter.next()) + " ");
		}
		System.out.println();
		System.out.println();

		// Remove half the numbers
		Util.shuffle(numbers);
		for (int i = 0; i < 50; ++i) {
			s.remove(numbers.get(i));
		}
		s.print();
		System.out.println();

		// Test iteration again
		for (Iterator iter = s.iterator(); iter.hasNext();) {
			System.out.print(iter.next() + " ");
		}
		System.out.println();

		// Test get method again
		for (int i = 0; i < 50; ++i) {
			System.out.print(s.get(i) + " ");
		}
		System.out.println();

		// Test indexOf method again
		for (Iterator iter = s.iterator(); iter.hasNext();) {
			System.out.print(s.indexOf(iter.next()) + " ");
		}
		System.out.println();
	}
}
