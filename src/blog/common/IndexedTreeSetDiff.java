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
 * Implementation of the IndexedSortedSet interface that is represented as a set
 * of differences from an underlying IndexedSortedSet.
 * 
 * <p>
 * An IndexedSetDiff will behave correctly if objects are added to the
 * underlying set after the IndexedSetDiff is created, as long as these objects
 * have not also been added to the IndexedSetDiff (then they would be
 * double-counted). Removing objects from the underlying set may cause an
 * IndexedSetDiff to behave incorrectly, because it may change the indices of
 * elements of the underlying set.
 */
public class IndexedTreeSetDiff extends AbstractSet implements
		IndexedSortedSet, IndexedSetDiff {
	/**
	 * Creates a new IndexedTreeSetDiff with the given underlying
	 * IndexedSortedSet. This set uses the same comparator as the underlying set.
	 */
	public IndexedTreeSetDiff(IndexedSortedSet underlying) {
		this.underlying = underlying;
		this.comparator = underlying.comparator();
		initStructures();
	}

	public int size() {
		return (underlying.size() - removedObjs.size() + additions.size());
	}

	public Iterator iterator() {
		return new IndexedTreeSetDiffIterator();
	}

	public boolean contains(Object o) {
		return (additions.contains(o) || (underlying.contains(o) && !removedObjs
				.contains(o)));
	}

	public boolean add(Object o) {
		if (underlying.contains(o)) {
			if (removedObjs.contains(o)) {
				removedObjs.remove(o);
				removeFromBlock(o);
				return true;
			}
			return false;
		}

		boolean added = additions.add(o);
		if (added) {
			addToBlock(o, false);
		}
		return added;
	}

	public boolean remove(Object o) {
		if (underlying.contains(o)) {
			// Removing an object that is in the underlying set
			boolean removed = removedObjs.add(o);
			if (removed) {
				addToBlock(o, true);
			}
			return removed;
		}

		boolean removed = additions.remove(o);
		if (removed) {
			removeFromBlock(o);
		}
		return removed;
	}

	public Object get(int index) {
		if ((index < 0) || (index >= size())) {
			throw new NoSuchElementException("Set of size " + size()
					+ " has no element at " + index);
		}

		int numSoFar = 0;
		Object prevBlockLastObj = null;
		int inclusionAdj = 0;

		for (Iterator iter = firstObjToBlock.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			Block block = (Block) entry.getValue();

			// See if index is in the fragment of the underlying set
			// immediately before this block.
			SortedSet underlyingFragment = ((prevBlockLastObj == null) ? underlying
					.headSet(block.first) : underlying.subSet(prevBlockLastObj,
					block.first));
			int fragmentSize = underlyingFragment.size() - inclusionAdj;
			if (index < numSoFar + fragmentSize) {
				int fragStart = (underlying.indexOf(underlyingFragment.first()) + inclusionAdj);
				int indexInUnderlying = fragStart + (index - numSoFar);
				return underlying.get(indexInUnderlying);
			}
			numSoFar += fragmentSize;

			// See if index is in this block
			if (!block.isRemoval) {
				if (index < numSoFar + block.size) {
					int blockStart = additions.indexOf(block.first);
					int indexInAdditions = blockStart + (index - numSoFar);
					return additions.get(indexInAdditions);
				}
				numSoFar += block.size;
			}

			// Adjust for the fact that block.last is in the underlying
			// set if and only if the block is a removal block.
			inclusionAdj = (block.isRemoval ? 1 : 0);

			prevBlockLastObj = block.last;
		}

		// index is after the end of the last block
		SortedSet underlyingFragment = ((prevBlockLastObj == null) ? underlying
				: underlying.tailSet(prevBlockLastObj));
		int fragmentSize = underlyingFragment.size() - inclusionAdj;
		int fragStart = (underlying.indexOf(underlyingFragment.first()) + inclusionAdj);
		int indexInUnderlying = fragStart + (index - numSoFar);
		return underlying.get(indexInUnderlying);
	}

	public int indexOf(Object o) {
		if (underlying.contains(o) && !removedObjs.contains(o)) {
			return (underlying.indexOf(o) + additions.headSet(o).size() - removedObjs
					.headSet(o).size());
		} else if (additions.contains(o)) {
			return (additions.indexOf(o) + underlying.headSet(o).size() - removedObjs
					.headSet(o).size());
		}
		return -1;
	}

	public Comparator comparator() {
		return comparator;
	}

	public Object first() {
		return get(0);
	}

	public Object last() {
		return get(size() - 1);
	}

	public SortedSet headSet(Object toElement) {
		throw new UnsupportedOperationException();
	}

	public SortedSet tailSet(Object fromElement) {
		throw new UnsupportedOperationException();
	}

	public SortedSet subSet(Object fromElement, Object toElement) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns the set of objects that are in this set and not the underlying set.
	 */
	public Set getAdditions() {
		return Collections.unmodifiableSet(additions);
	}

	/**
	 * Returns the set of objects that are in the underlying set but not in this
	 * set.
	 */
	public Set getRemovals() {
		return Collections.unmodifiableSet(removedObjs);
	}

	/**
	 * Changes the underlying IndexedSet so it is equal to this one, and clears
	 * the changes in this IndexedSetDiff. Note that his operation may change the
	 * indices of some elements.
	 */
	public void changeUnderlying() {
		for (Iterator iter = removedObjs.iterator(); iter.hasNext();) {
			underlying.remove(iter.next());
		}
		for (Iterator iter = additions.iterator(); iter.hasNext();) {
			underlying.add(iter.next());
		}

		clearChanges();
	}

	/**
	 * Clears the changes in this IndexedSetDiff so it is once again equal to the
	 * underlying IndexedSet.
	 */
	public void clearChanges() {
		initStructures();
	}

	private void addToBlock(Object o, boolean isRemoval) {
		// Find last block whose first object is <= o
		Block block = (Block) firstObjToBlock.get(o);
		if (block == null) {
			// o is not the first object in any block
			SortedMap headMap = firstObjToBlock.headMap(o);
			if (!headMap.isEmpty()) {
				block = (Block) headMap.get(headMap.lastKey());
			}
		}

		if (block != null) {
			if (compare(o, block.last) <= 0) {
				// o falls into this block. The block should have the
				// same removal flag.
				if (block.isRemoval != isRemoval) {
					throw new IllegalStateException("Object " + o + " with isRemoval = "
							+ isRemoval + " landed in block with different isRemoval flag: "
							+ block);
				}
				++block.size;
				return;
			}

			// Existing block ends before o. If there are no other
			// underlying objects between block.last and o, then we can
			// extend the block to include o.
			if ((isRemoval && block.isRemoval && (underlying.subSet(block.last, o)
					.size() == 1))
					|| (!isRemoval && !block.isRemoval && (underlying.subSet(block.last,
							o).size() == 0))) {
				block.last = o;
				++block.size;
				return;
			}
		}

		// Can't put o in existing block. See if we can extend the
		// next block backward to include o.
		SortedMap tailMap = firstObjToBlock.tailMap(o);
		if (!tailMap.isEmpty()) {
			Block nextBlock = (Block) tailMap.get(tailMap.firstKey());
			if ((isRemoval && nextBlock.isRemoval && (underlying.subSet(o,
					nextBlock.first).size() == 1))
					|| (!isRemoval && !nextBlock.isRemoval && (underlying.subSet(o,
							nextBlock.first).size() == 0))) {
				firstObjToBlock.remove(nextBlock.first);
				nextBlock.first = o;
				++nextBlock.size;
				firstObjToBlock.put(o, nextBlock);
				return;
			}
		}

		// Have to create new block for o.
		Block newBlock = new Block(o, o, 1, isRemoval);
		firstObjToBlock.put(o, newBlock);
	}

	private void removeFromBlock(Object o) {
		// Find block containing o
		Block block = (Block) firstObjToBlock.get(o);
		if (block == null) {
			// o is not the first object in its block
			SortedMap headMap = firstObjToBlock.headMap(o);
			if (!headMap.isEmpty()) {
				block = (Block) headMap.get(headMap.lastKey());
			}
		}

		if (block == null) {
			throw new IllegalArgumentException("Object " + o
					+ " is not in any block.");
		}

		--block.size;
		if (block.size == 0) {
			firstObjToBlock.remove(block.first);
		} else {
			SortedSet container = (block.isRemoval ? removedObjs : additions);
			// assume o has already been removed from container

			if (block.first.equals(o)) {
				firstObjToBlock.remove(block.first);
				block.first = container.tailSet(o).first();
				firstObjToBlock.put(block.first, block);
			} else if (block.last.equals(o)) {
				block.last = container.headSet(o).last();
			}
		}
	}

	private void initStructures() {
		additions = new IndexedTreeSet(comparator);
		removedObjs = new IndexedTreeSet(comparator);
		firstObjToBlock = new TreeMap(comparator);
	}

	private int compare(Object o1, Object o2) {
		if (comparator == null) {
			return ((Comparable) o1).compareTo(o2);
		}
		return comparator.compare(o1, o2);
	}

	private class IndexedTreeSetDiffIterator implements Iterator {
		IndexedTreeSetDiffIterator() {
			underlyingIter = underlying.iterator();
			loadNextFromUnderlying();

			additionsIter = additions.iterator();
			if (additionsIter.hasNext()) {
				nextFromAdditions = additionsIter.next();
			}
		}

		public boolean hasNext() {
			return ((nextFromUnderlying != null) || (nextFromAdditions != null));
		}

		public Object next() {
			if ((nextFromUnderlying != null)
					&& ((nextFromAdditions == null) || (compare(nextFromUnderlying,
							nextFromAdditions) <= 0))) {
				latestFromUnderlying = true;
				latestObj = nextFromUnderlying;
				loadNextFromUnderlying();
				return latestObj;
			} else if (nextFromAdditions != null) {
				latestFromUnderlying = false;
				latestObj = nextFromAdditions;
				nextFromAdditions = (additionsIter.hasNext() ? additionsIter.next()
						: null);
				return latestObj;
			}

			throw new NoSuchElementException();
		}

		public void remove() {
			if (latestObj == null) {
				throw new IllegalStateException(
						"next has not been called since last call to remove.");
			}

			if (latestFromUnderlying) {
				// Removing an object that was in the underlying set.
				// Don't need to worry about iterator because we aren't
				// actually modifying the underlying set.
				IndexedTreeSetDiff.this.remove(latestObj);
			} else {
				additionsIter.remove();
			}

			latestObj = null;
		}

		private void loadNextFromUnderlying() {
			while (underlyingIter.hasNext()) {
				nextFromUnderlying = underlyingIter.next();
				if (!removedObjs.contains(nextFromUnderlying)) {
					return; // got a valid object
				}
			}

			// must have reached end of underlying set
			nextFromUnderlying = null;
		}

		private Iterator underlyingIter;
		private Iterator additionsIter;
		private Object nextFromUnderlying = null;
		private Object nextFromAdditions = null;
		private Object latestObj = null;
		private boolean latestFromUnderlying = false;
	}

	private static class Block {
		Block(Object first, Object last, int size, boolean isRemoval) {
			this.first = first;
			this.last = last;
			this.size = size;
			this.isRemoval = isRemoval;
		}

		public String toString() {
			return ("[" + first + ", " + last + ", " + isRemoval + "]");
		}

		Object first;
		Object last;
		int size;
		boolean isRemoval;
	}

	private Comparator comparator;
	private IndexedSortedSet underlying;
	private IndexedSortedSet additions;
	private IndexedSortedSet removedObjs;
	private SortedMap firstObjToBlock;

	/**
	 * Test program.
	 */
	public static void main(String[] args) {
		IndexedSortedSet underlying = new IndexedTreeSet();
		for (int i = 0; i < 5; ++i) {
			underlying.add(new Integer(i));
		}
		System.out.println("underlying: " + underlying); // [0 1 2 3 4]

		IndexedTreeSetDiff diff = new IndexedTreeSetDiff(underlying);
		System.out.println("diff: " + diff); // [0 1 2 3 4]

		diff.add(new Integer(5));
		diff.add(new Integer(6));
		System.out.println("diff: " + diff); // [0 1 2 3 4 5 6]
		System.out.println("underlying: " + underlying); // [0 1 2 3 4]

		diff.remove(new Integer(5));
		System.out.println("diff: " + diff); // [0 1 2 3 4 6]

		diff.remove(new Integer(1));
		diff.remove(new Integer(3));
		System.out.println("diff: " + diff); // [0 2 4 6]

		System.out.println();
		System.out.println("indexOf(0): " + diff.indexOf(new Integer(0))); // 0
		System.out.println("indexOf(2): " + diff.indexOf(new Integer(2))); // 1
		System.out.println("indexOf(4): " + diff.indexOf(new Integer(4))); // 2
		System.out.println("indexOf(6): " + diff.indexOf(new Integer(6))); // 3
		System.out.println("indexOf(1): " + diff.indexOf(new Integer(1))); // -1

		System.out.println();
		System.out.println("get(0): " + diff.get(0)); // 0
		System.out.println("get(1): " + diff.get(1)); // 2
		System.out.println("get(2): " + diff.get(2)); // 4
		System.out.println("get(3): " + diff.get(3)); // 6
	}
}
