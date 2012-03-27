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

package common;

import java.util.*;

/**
 * A Fibonacci heap data structure, as described in Chapter 20 of Cormen,
 * Leiserson, Rivest & Stein (2001).
 */
public class FibHeap extends AbstractCollection implements Heap {
	/**
	 * Creates a new, empty Fibonnaci heap.
	 */
	public FibHeap() {
	}

	public int size() {
		return heapSize;
	}

	public boolean isEmpty() {
		return (heapSize == 0);
	}

	public void clear() {
		roots.clear();
		minNode = null;
		heapSize = 0;
	}

	/**
	 * Returns an iterator over the Heap.Entry objects in this heap. The iteration
	 * is not guaranteed to be in any particular order. The iterator does not
	 * support removal.
	 */
	public Iterator iterator() {
		return new HeapIterator(null, roots);
	}

	public Heap.Entry peekMin() {
		if (minNode == null) {
			throw new NoSuchElementException("Can't get minimum node of empty heap.");
		}
		return minNode;
	}

	public Heap.Entry extractMin() {
		if (minNode == null) {
			throw new NoSuchElementException("Can't get minimum node of empty heap.");
		}

		if (minNode.children != null) {
			for (Iterator iter = minNode.children.iterator(); iter.hasNext();) {
				makeRoot((Node) iter.next());
			}
		}
		minNode.parent = REMOVED;
		--heapSize;
		Node toReturn = minNode;
		minNode = null;

		// Rearrange trees so there is at most one root of each degree
		for (Iterator iter = roots.iterator(); iter.hasNext();) {
			Node root = (Node) iter.next();
			if (root != toReturn) {
				handleRootOfDegree(root, root.degree());
			}
		}

		// Reconstruct root list and find new minimum
		roots.clear();
		for (Iterator iter = rootOfDegree.iterator(); iter.hasNext();) {
			Node root = (Node) iter.next();
			if (root != null) {
				roots.add(root);
				if ((minNode == null) || (root.cost < minNode.cost)) {
					minNode = root;
				}
			}
		}

		rootOfDegree.clear();

		return toReturn;
	}

	public Heap.Entry add(Object o, double cost) {
		Node node = new Node(o, cost);
		roots.add(node);
		if ((minNode == null) || (cost < minNode.cost)) {
			minNode = node;
		}
		++heapSize;
		return node;
	}

	/**
	 * Decreases the cost for the given entry. The entry must already be in this
	 * heap, and the new cost must be less than or equal to its current cost.
	 * 
	 * <p>
	 * Warning: If the new cost is greater than the current one, this method will
	 * not produce an error message, but the heap may behave incorrectly.
	 */
	public void decreaseCost(Heap.Entry entry, double newCost) {
		Node node = (Node) entry;
		if (node.parent == REMOVED) {
			throw new IllegalArgumentException("Heap entry " + entry
					+ " has been removed.");
		}
		node.cost = newCost;
		if (newCost < minNode.cost) {
			minNode = node;
		}

		if ((node.parent != null) && (node.cost < node.parent.cost)) {
			// Make this node, and possibly some of its ancestors,
			// into roots
			Node toMove = node;
			while (true) {
				Node parent = toMove.parent;
				makeRoot(toMove);
				if ((parent == null) || !parent.removeChild(toMove)) {
					break;
				}
				toMove = parent;
			}
		}
	}

	/**
	 * Throws an UnsupportedOperationException.
	 */
	public void changeCost(Heap.Entry entry, double newCost) {
		throw new UnsupportedOperationException(
				"changeCost not implemented for FibHeap.");
	}

	private void handleRootOfDegree(Node curRoot, int degree) {
		while ((degree < rootOfDegree.size()) && (rootOfDegree.get(degree) != null)) {
			// Two nodes both have given degree. One will become
			// a child of the other, so one ceases to be a root
			// and the other has its degree increased.
			Node existing = (Node) rootOfDegree.get(degree);
			if (existing.cost <= curRoot.cost) {
				existing.addChild(curRoot);
				curRoot = existing;
			} else {
				curRoot.addChild(existing);
			}
			rootOfDegree.set(degree, null);
			++degree;
		}
		setRootOfDegree(degree, curRoot);
	}

	private void setRootOfDegree(int degree, Node root) {
		for (int i = rootOfDegree.size(); i < degree; ++i) {
			rootOfDegree.add(null);
		}
		if (degree == rootOfDegree.size()) {
			rootOfDegree.add(root);
		} else {
			rootOfDegree.set(degree, root);
		}
	}

	private void makeRoot(Node node) {
		node.parent = null;
		node.marked = false;
		roots.add(node);
	}

	private static class Node implements Heap.Entry {
		Node(Object element, double cost) {
			this.element = element;
			this.cost = cost;
		}

		public Object getElement() {
			return element;
		}

		public double getCost() {
			return cost;
		}

		public String toString() {
			return (element + ": " + cost);
		}

		int degree() {
			return (children == null) ? 0 : children.size();
		}

		List children() {
			return (children == null) ? Collections.EMPTY_LIST : children;
		}

		void addChild(Node child) {
			if (children == null) {
				children = new LinkedList();
			}
			children.add(child);
			child.parent = this;
		}

		/**
		 * Returns true if this node should become a root too so the heap can be
		 * rebalanced.
		 */
		boolean removeChild(Node child) {
			children.remove(child); // TODO: speed this up
			if (parent != null) {
				if (marked) {
					return true;
				}
				marked = true;
			}
			return false;
		}

		Object element;
		double cost;
		boolean marked;
		Node parent = null;
		LinkedList children = null;
	}

	private static class HeapIterator implements Iterator {
		HeapIterator(Object firstNode, List children) {
			childIter = children.iterator();
			next = (firstNode == null) ? getNext() : firstNode;
		}

		public boolean hasNext() {
			return (next != null);
		}

		public Object next() {
			if (next == null) {
				throw new NoSuchElementException();
			}

			Object toReturn = next;
			next = getNext();
			return toReturn;
		}

		public void remove() {
			throw new UnsupportedOperationException(
					"Can't remove from FibHeap iterator.");
		}

		private Object getNext() {
			if ((subtreeIter == null) || !subtreeIter.hasNext()) {
				if (childIter.hasNext()) {
					Node child = (Node) childIter.next();
					subtreeIter = new HeapIterator(child, child.children());
				} else {
					return null;
				}
			}
			return subtreeIter.next();
		}

		Iterator childIter;
		Iterator subtreeIter = null;
		Object next;
	}

	private List roots = new LinkedList(); // of Node
	private Node minNode = null;
	private int heapSize = 0;

	private List rootOfDegree = new ArrayList(); // of Node

	private static final Node REMOVED = new Node(null, 0);

	/**
	 * Test program.
	 */
	public static void main(String[] args) {
		Util.initRandom(false);

		// Fill heap with numbers associated with random costs
		Heap heap = new FibHeap();
		Heap.Entry[] entries = new Heap.Entry[100];
		for (int i = 0; i < entries.length; ++i) {
			System.out.println("Adding element " + i);
			entries[i] = heap.add(String.valueOf(i), Util.random());
		}
		System.out.println();

		// Extract half the entries
		for (int i = 0; i < 50; ++i) {
			Heap.Entry entry = heap.extractMin();
			System.out.println(entry);
		}
		System.out.println();

		// Decrease costs of remaining entries by a random amount
		for (Iterator iter = new ArrayList(heap).iterator(); iter.hasNext();) {
			Heap.Entry entry = (Heap.Entry) iter.next();
			System.out.println("Decreasing cost for element " + entry.getElement());
			heap.decreaseCost(entry, entry.getCost() - Util.random());
		}
		System.out.println();

		System.out.println("Entries in iteration order:");
		int numPrinted = 0;
		for (Iterator iter = heap.iterator(); iter.hasNext();) {
			System.out.println(iter.next());
			++numPrinted;
		}
		System.out.println("Number of entries printed: " + numPrinted);

		System.out.println("\nEntries in sorted order:");
		numPrinted = 0;
		while (!heap.isEmpty()) {
			Heap.Entry entry = heap.extractMin();
			System.out.println(entry);
			++numPrinted;
		}
		System.out.println("Number of entries printed: " + numPrinted);
	}
}
