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

package blog.common;

import java.util.*;

/**
 * An unmodifiable Multiset implementation that just wraps around a Set object.
 */
public class SetBackedMultiset extends AbstractMultiset {
	/**
	 * Creates a new SetBackedMultiset that will be backed by the given set. If
	 * the contents of the given set change, the contents of this
	 * SetBackedMultiset will change too.
	 */
	public SetBackedMultiset(Set s) {
		this.s = Collections.unmodifiableSet(s);
	}

	/**
	 * Returns the number of elements in this multiset.
	 */
	public int size() {
		return s.size();
	}

	/**
	 * Returns the number of occurrences of the given element in this multiset.
	 */
	public int count(Object o) {
		return (s.contains(o) ? 1 : 0);
	}

	/**
	 * Returns an iterator over this multiset. If the multiset contains n copies
	 * of an element, the iterator will return that element n times.
	 */
	public Iterator iterator() {
		return s.iterator();
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

	/**
	 * Inner class representing the set of entries in this multiset. It is just an
	 * adapter backed by the multiset's underlying set.
	 */
	private class EntrySet extends AbstractSet {
		public boolean contains(Object o) {
			if (o instanceof Multiset.Entry) {
				Multiset.Entry entry = (Multiset.Entry) o;
				return (s.contains(entry.getElement()) && (entry.getCount() == 1));
			}
			return false;
		}

		public int size() {
			return s.size();
		}

		public Iterator iterator() {
			return new EntrySetIterator();
		}

		/**
		 * Inner class that just wraps around a Set iterator and wraps all the
		 * elements in Multiset.Entry objects.
		 */
		private class EntrySetIterator implements Iterator {
			public boolean hasNext() {
				return setIter.hasNext();
			}

			public Object next() {
				return new SetBackedMultiset.Entry(setIter.next());
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}

			private Iterator setIter = s.iterator();
		}
	}

	/**
	 * Implementation of Multiset.Entry that always returns 1 as the count.
	 */
	private static class Entry extends AbstractMultiset.Entry {
		public Entry(Object o) {
			element = o;
		}

		public Object getElement() {
			return element;
		}

		public int getCount() {
			return 1;
		}

		private Object element;
	}

	private Set s;
	private EntrySet entrySet = new EntrySet(); // inner class backed by s
}
