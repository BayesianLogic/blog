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
 * An unmodifiable Set that is backed by a Multiset. The elements in the set are
 * the distinct elements of the multiset.
 */
public class MultisetBackedSet extends AbstractSet {
	/**
	 * Creates a new MultisetBackedSet backed by the given multiset.
	 */
	public MultisetBackedSet(Multiset s) {
		underlying = s;
	}

	/**
	 * Returns the size of this set, which is the number of distinct elements in
	 * the underlying multiset.
	 */
	public int size() {
		return underlying.entrySet().size();
	}

	/**
	 * Returns true if this set contains the given element, that is, if the
	 * underlying multiset contains at least one occurrence of that element.
	 */
	public boolean contains(Object o) {
		return underlying.contains(o);
	}

	/**
	 * Returns an iterator over the elements of this set. This is like calling
	 * entrySet().iterator() on the underlying multiset, except that the objects
	 * returned by the iterator are the elements themselves, not Multiset.Entry
	 * objects.
	 */
	public Iterator iterator() {
		return new WrappingIterator();
	}

	private class WrappingIterator implements Iterator {
		public boolean hasNext() {
			return entryIter.hasNext();
		}

		public Object next() {
			return ((Multiset.Entry) entryIter.next()).getElement();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

		private Iterator entryIter = underlying.entrySet().iterator();
	}

	private Multiset underlying;
}
