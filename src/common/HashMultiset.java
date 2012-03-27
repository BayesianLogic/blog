/*
 * Copyright (c) 2005, Regents of the University of California
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
 * An implementation of the Multiset interface that uses a HashMap from elements
 * to integers as the underlying data structure.
 */
public class HashMultiset extends AbstractMultiset {
	/**
	 * Creates an empty HashMultiset.
	 */
	public HashMultiset() {
	}

	/**
	 * Creates a HashMultiset with the same multiset of elements as the given
	 * Collection.
	 */
	public HashMultiset(Collection c) {
		for (Iterator iter = c.iterator(); iter.hasNext();) {
			add(iter.next());
		}
	}

	/**
	 * Returns the sum of the occurrence counts of all items.
	 */
	public int size() {
		return size;
	}

	/**
	 * Returns the number of occurrences of the given element in this multiset.
	 */
	public int count(Object o) {
		Object v = map.get(o);
		if (v == null) {
			return 0;
		}
		return ((Integer) v).intValue();
	}

	/**
	 * Adds a single copy of the given element to this multiset.
	 * 
	 * @return true in all cases
	 */
	public boolean add(Object o) {
		int numCopies = 1;
		Object v = map.get(o);
		if (v != null) {
			numCopies += ((Integer) v).intValue();
		}
		map.put(o, new Integer(numCopies));
		size++;
		return true;
	}

	/**
	 * Removes a single copy of the given element from this multiset, if such an
	 * element is present.
	 * 
	 * @return true if the given element was present
	 */
	public boolean remove(Object o) {
		Object v = map.get(o);
		if (v == null) {
			return false;
		}

		int numCopies = ((Integer) v).intValue() - 1;
		if (numCopies == 0) {
			map.remove(o);
		} else {
			map.put(o, new Integer(numCopies));
		}
		size--;
		return true;
	}

	/**
	 * Removes all elements from this multiset.
	 */
	public void clear() {
		map.clear();
		size = 0;
	}

	/**
	 * Returns an iterator over this multiset. If the multiset contains n copies
	 * of an element, the iterator will return that element n times.
	 */
	public Iterator iterator() {
		return new MultisetIterator();
	}

	/**
	 * Returns the set of entries in the multiset. An entry is a pair (e, n) where
	 * e is an element of the multiset and n is the number of times e occurs in
	 * the multiset. The returned set contains exactly one entry for each distinct
	 * element e. Thus, entrySet.size() returns the number of distinct elements in
	 * the multiset.
	 */
	public Set entrySet() {
		return entrySet;
	}

	private class MultisetIterator implements Iterator {
		public boolean hasNext() {
			return (mapIter.hasNext() || (numCopiesLeft > 0));
		}

		public Object next() {
			if (numCopiesLeft > 0) {
				numCopiesLeft--;
				justRemoved = false;
				return lastEntry.getKey();
			}

			lastEntry = (Map.Entry) mapIter.next();
			numCopiesLeft = ((Integer) lastEntry.getValue()).intValue() - 1;
			justRemoved = false;
			return lastEntry.getKey();
		}

		public void remove() {
			if ((lastEntry == null) || justRemoved) {
				throw new IllegalStateException(
						"Illegal call to remove() on HashMultiset iterator.");
			}

			int numCopies = ((Integer) lastEntry.getValue()).intValue() - 1;
			if (numCopies == 0) {
				mapIter.remove();
				lastEntry = null;
			} else {
				lastEntry.setValue(new Integer(numCopies));
			}
			size--;
			justRemoved = true;
		}

		private Iterator mapIter = map.entrySet().iterator();

		/**
		 * The map entry corresponding to the last element returned by this
		 * iterator. Set to null if next() has not been called yet or all copies of
		 * the last element have been removed.
		 */
		private Map.Entry lastEntry = null;

		/**
		 * The number of copies of the last element returned by this iterator that
		 * we have left to return. Note that deleting a previously returned copy
		 * does not affect this value.
		 */
		private int numCopiesLeft = 0;

		/**
		 * True if this iterator has not returned any elements through the next()
		 * method since the last successful call to remove().
		 */
		private boolean justRemoved = false;
	}

	/**
	 * Inner class representing the set of entries in this multiset. It is just an
	 * adapter backed by the multiset's hash map.
	 */
	private class EntrySet extends AbstractSet {
		public boolean contains(Object o) {
			if (o instanceof Multiset.Entry) {
				Multiset.Entry entry = (Multiset.Entry) o;
				Integer count = (Integer) map.get(entry.getElement());
				if (count != null) {
					return (entry.getCount() == count.intValue());
				}
			}
			return false;
		}

		public int size() {
			return map.size();
		}

		public Iterator iterator() {
			return new EntrySetIterator();
		}

		public boolean remove(Object o) {
			Integer count = (Integer) map.get(o);
			if (count == null) {
				return false;
			}

			map.remove(o);
			size -= count.intValue();
			return true;
		}

		/**
		 * Inner class that just wraps around a HashMap iterator and changes all the
		 * Map.Entry objects to Multiset.Entry objects.
		 */
		private class EntrySetIterator implements Iterator {
			public boolean hasNext() {
				return mapIter.hasNext();
			}

			public Object next() {
				lastEntry = new HashMultiset.Entry((Map.Entry) mapIter.next());
				return lastEntry;
			}

			public void remove() {
				if (lastEntry == null) {
					throw new IllegalStateException(
							"Illegal call to remove() on HashMultiset iterator.");
				}

				int numCopies = lastEntry.getCount();
				mapIter.remove();
				size -= numCopies;
				lastEntry = null;
			}

			private Iterator mapIter = map.entrySet().iterator();
			private Multiset.Entry lastEntry = null;
		}
	}

	private static class Entry extends AbstractMultiset.Entry {
		public Entry(Map.Entry mapEntry) {
			this.mapEntry = mapEntry;
		}

		public Object getElement() {
			return mapEntry.getKey();
		}

		public int getCount() {
			return ((Integer) mapEntry.getValue()).intValue();
		}

		private Map.Entry mapEntry;
	}

	private Map map = new HashMap();
	private int size = 0;
	private Set entrySet = new EntrySet(); // inner class backed by map

	/**
	 * Test program
	 */
	public static void main(String[] args) {
		System.out.println("Multiset: a a b c c c d d");
		Multiset m = new HashMultiset();
		m.add("a");
		m.add("a");
		m.add("b");
		m.add("c");
		m.add("c");
		m.add("c");
		m.add("d");
		m.add("d");
		System.out.println("Size: " + m.size()); // should be 8
		// should be 4
		System.out.println("Num distinct: " + m.entrySet().size());
		System.out.println();
		System.out.println("All elements:");
		// should be some permutation of: [a a b c c c d d]
		System.out.println(m);

		System.out.println();
		System.out.println("Distinct elements:");
		// should be some permutation of: a(2) b(1) c(3) d(2)
		for (Iterator iter = m.entrySet().iterator(); iter.hasNext();) {
			Multiset.Entry e = (Multiset.Entry) iter.next();
			System.out.print(e.getElement() + "(" + e.getCount() + ") ");
		}
		System.out.println();

		System.out.println();
		System.out.println("Removing one 'a'.");
		m.remove("a");
		System.out.println("Size: " + m.size()); // should be 7
		System.out.println("All elements:");
		// should be some permutation of: [a b c c c d d]
		System.out.println(m);

		System.out.println();
		System.out.println("Removing the lone 'b'.");
		m.remove("b");
		System.out.println("Size: " + m.size()); // should be 6
		System.out.println("All elements:");
		// should be some permutation of: [a c c c d d]
		System.out.println(m);

		System.out.println();
		System.out.println("Iterating over elements, removing remaining 'a' "
				+ "and last 'd'.");
		boolean gotD = false;
		for (Iterator iter = m.iterator(); iter.hasNext();) {
			Object o = iter.next();
			if (o.equals("a")) {
				iter.remove();
			}
			if (o.equals("d")) {
				if (gotD) {
					iter.remove();
				} else {
					gotD = true;
				}
			}
		}
		System.out.println("Size: " + m.size()); // should be 4
		System.out.println("All elements:");
		// Should be some permutation of: [c c c d]
		System.out.println(m);

		System.out.println();
		System.out.println("Iterating over distinct elements, removing 'c'.");
		for (Iterator iter = m.entrySet().iterator(); iter.hasNext();) {
			Multiset.Entry e = (Multiset.Entry) iter.next();
			if (e.getElement().equals("c")) {
				iter.remove();
			}
		}
		System.out.println("Size: " + m.size()); // should be 1
		System.out.println("All elements:");
		// Should be: [d]
		System.out.println(m);

		System.out.println();
		Multiset q = new HashMultiset();
		q.add("d");
		System.out.println("Multiset q: " + q);
		System.out.println("m equals q: " + m.equals(q)); // should be true
	}
}
