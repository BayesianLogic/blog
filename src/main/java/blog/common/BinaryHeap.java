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
 * A binary heap data structure, as described in Cormen, Leiserson & Rivest
 * (1990) Chapter 7.
 * 
 * <p>
 * Implementation note: indices in the heap are 1-based.
 */
public class BinaryHeap extends AbstractCollection implements Heap {
	/**
	 * Creates a new, empty BinaryHeap.
	 */
	public BinaryHeap() {
	}

	/**
	 * Returns the number of entries in this heap.
	 */
	public int size() {
		return heapSize;
	}

	/**
	 * Returns true if this heap has no entries.
	 */
	public boolean isEmpty() {
		return (heapSize == 0);
	}

	/**
	 * Returns an iterator over the Heap.Entry objects in this heap. The iteration
	 * is not guaranteed to be in any particular order. The iterator does not
	 * support removal.
	 */
	public Iterator iterator() {
		return new HeapIterator();
	}

	/**
	 * Returns the minimum-cost entry in this heap, without modifying the heap.
	 * 
	 * @throws NoSuchElementException
	 *           if the heap is empty
	 */
	public Heap.Entry peekMin() {
		if (heapSize == 0) {
			throw new NoSuchElementException("Heap is empty.");
		}
		return entries[1];
	}

	/**
	 * Returns the minimum-cost entry in this heap and removes it from the heap.
	 * 
	 * @throws NoSuchElementException
	 *           if the heap is empty
	 */
	public Heap.Entry extractMin() {
		if (heapSize == 0) {
			throw new NoSuchElementException("Heap is empty.");
		}
		Entry min = entries[1];
		moveToIndex(entries[heapSize], 1);
		--heapSize;
		heapify(1);
		return min;
	}

	/**
	 * Adds an entry to this heap for the given object and cost. This does not
	 * affect any other entries for the same object. Returns the entry that was
	 * added.
	 */
	public Heap.Entry add(Object o, double cost) {
		DefaultEntry entry = new DefaultEntry(o, cost);
		incrementSize();
		moveUpAsNecessary(entry, heapSize);
		return entry;
	}

	/**
	 * Removes all elements from this heap.
	 */
	public void clear() {
		heapSize = 0;
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
		int index = ((DefaultEntry) entry).index;
		if ((index > heapSize) || (entries[index] != entry)) {
			throw new IllegalArgumentException("Entry is not in this heap: "
					+ entry.getElement());
		}
		((DefaultEntry) entry).cost = newCost;
		moveUpAsNecessary((DefaultEntry) entry, index);
	}

	public void changeCost(Heap.Entry entry, double newCost) {
		DefaultEntry e = (DefaultEntry) entry;
		int index = e.index;
		if ((index > heapSize) || (entries[index] != entry)) {
			throw new IllegalArgumentException("Entry is not in this heap: "
					+ e.getElement());
		}

		double oldCost = e.cost;
		e.cost = newCost;
		if (newCost < oldCost) {
			moveUpAsNecessary(e, index);
		} else if (newCost != oldCost) {
			heapify(index);
		}
	}

	private void heapify(int index) {
		DefaultEntry entry = entries[index];
		int left = left(index);
		int right = right(index);

		int smallest = index;
		if ((left <= heapSize) && (entries[left].cost < entries[smallest].cost)) {
			smallest = left;
		}
		if ((right <= heapSize) && (entries[right].cost < entries[smallest].cost)) {
			smallest = right;
		}

		if (smallest != index) {
			moveToIndex(entries[smallest], index);
			moveToIndex(entry, smallest);
			heapify(smallest);
		}
	}

	private void moveToIndex(DefaultEntry entry, int newIndex) {
		entries[newIndex] = entry;
		entry.index = newIndex;
	}

	private void incrementSize() {
		++heapSize;
		if (heapSize == entries.length) {
			DefaultEntry[] newEntries = new DefaultEntry[entries.length * 2];
			System.arraycopy(entries, 1, newEntries, 1, entries.length - 1);
			entries = newEntries;
		}
	}

	private void moveUpAsNecessary(DefaultEntry entry, int index) {
		while (index > 1) {
			int parent = parent(index);
			if (entries[parent].cost <= entry.cost) {
				break;
			}
			moveToIndex(entries[parent], index);
			index = parent;
		}
		moveToIndex(entry, index);
	}

	private boolean checkHeap() {
		for (int index = 2; index <= heapSize; ++index) {
			if (entries[index].cost < entries[parent(index)].cost) {
				System.out.println("Heap check failed: entry " + entries[index]
						+ " has lower cost than " + "its parent " + entries[parent(index)]);
				return false;
			}
		}
		return true;
	}

	// Note: to make these methods more efficient, indices in the heap
	// are 1-based.

	private static int parent(int index) {
		return (index >> 1);
	}

	private static int left(int index) {
		return (index << 1);
	}

	private static int right(int index) {
		return (index << 1) + 1;
	}

	private class HeapIterator implements Iterator {
		public boolean hasNext() {
			return (index <= heapSize);
		}

		public Object next() {
			if (index > heapSize) {
				throw new NoSuchElementException();
			}
			return entries[index++];
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

		private int index = 1;
	}

	private static class DefaultEntry implements Heap.Entry {
		private DefaultEntry(Object elt, double cost) {
			this.element = elt;
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

		private Object element;
		private double cost;
		private int index;
	}

	private DefaultEntry[] entries = new DefaultEntry[8]; // index 0 not used
	private int heapSize = 0;

	/**
	 * Test program.
	 */
	public static void main(String[] args) {
		Util.initRandom(false);

		// Fill heap with numbers associated with random costs
		BinaryHeap heap = new BinaryHeap();
		BinaryHeap.Entry[] entries = new BinaryHeap.Entry[100];
		for (int i = 0; i < entries.length; ++i) {
			System.out.println("Adding element " + i);
			entries[i] = heap.add(String.valueOf(i), Util.random());
			if (!heap.checkHeap()) {
				break;
			}
		}
		System.out.println();

		// Decrease each cost by a random amount
		for (int i = 0; i < entries.length; ++i) {
			System.out.println("Decreasing cost for element " + i);
			heap.decreaseCost(entries[i], entries[i].getCost() - Util.random());
			if (!heap.checkHeap()) {
				break;
			}
		}
		System.out.println();

		int numPrinted = 0;
		while (!heap.isEmpty()) {
			BinaryHeap.Entry entry = heap.extractMin();
			System.out.println(entry);
			++numPrinted;
			if (!heap.checkHeap()) {
				break;
			}
		}
		System.out.println("Number of entries printed: " + numPrinted);
	}
}
