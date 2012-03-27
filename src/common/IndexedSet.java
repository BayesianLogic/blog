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
 * Data structure that behaves like a set, except that its elements also have
 * consecutive indices starting at zero. Different implementations may assign
 * these indices in different ways: for example, according to the ordering on
 * elements defined by a Comparator object, or according to the order in which
 * objects were added. Indices are not guaranteed to remain stable when the
 * IndexedSet is modified. In some implementations, adding or removing elements
 * may change the indices of other elements.
 * 
 * <p>
 * IndexedSet extends the Set interface with additional methods <code>get</code>
 * and <code>indexOf</code>, which behave like the corresponding methods in the
 * List interface. Also, iterators over an IndexedSet go in the order defined by
 * the indices. The only thing that invalidates an iterator is removing an
 * element without going through the iterator's <code>remove</code> method.
 * Notably, adding elements does not invalidate iterators.
 * 
 * <p>
 * One advantage of using an IndexedSet is that you can sample an element
 * uniformly at random in constant time (by getting the element at a random
 * index <i>n</i>). Another is that you can iterate over it just by incrementing
 * an integer index.
 */
public interface IndexedSet extends Set {
	/**
	 * Returns the object with the specified index in this IndexedSet.
	 * 
	 * @throws IndexOutOfBoundsException
	 *           if the index is out of range (
	 *           <code>index < 0 || index >= size()</code>)
	 */
	Object get(int index);

	/**
	 * Returns the index of the given object in this IndexedSet, or -1 if this
	 * IndexedSet does not contain the given object.
	 */
	int indexOf(Object o);

	static class EmptyIndexedSet extends AbstractSet implements IndexedSet {
		public int size() {
			return 0;
		}

		public boolean contains(Object o) {
			return false;
		}

		public Iterator iterator() {
			return Collections.EMPTY_SET.iterator();
		}

		public Object get(int index) {
			throw new IndexOutOfBoundsException(
					"Empty indexed set has no element at index " + index);
		}

		public int indexOf(Object o) {
			return -1;
		}
	}

	/**
	 * An unmodifiable, empty indexed set.
	 */
	public static final IndexedSet EMPTY_INDEXED_SET = new EmptyIndexedSet();
}
