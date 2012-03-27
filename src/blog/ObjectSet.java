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

package blog;

import java.util.*;

/**
 * Interface for sets of objects in a partial world. Each ObjectSet is backed by
 * a particular partial world. It extends the Java Set interface in several
 * ways:
 * <ul>
 * <li><b>Sampling:</b> An ObjectSet has a <code>sample</code> method that
 * returns an element sampled uniformly at random. This may be faster than
 * iterating over the set until the <i>N</i>th element is reached (for a random
 * <i>N</i>).
 * 
 * <li><b>Indistinguishable objects:</b> The <code>iterator</code> method on an
 * ObjectSet returns an {@link blog.ObjectIterator ObjectIterator}, which has a
 * special method for skipping objects that are indistinguishable (in the
 * underlying partial world) from the last object returned. There is also an
 * iterator method that takes a set of <i>externally distinguished</i> objects
 * (typically the values of logical variables in the current scope), which are
 * treated as distinguishable from all other objects.
 * 
 * <li><b>Partially defined sets:</b> If a set is defined intensively (e.g., as
 * the set of objects satisfying a particular formula), then a partial world may
 * be complete enough to define the return values for certain methods on the set
 * (such as <code>isEmpty</code>) but not complete enough to define the set
 * fully. Thus, the ObjectSet interface includes methods
 * <code>canDetermineIsEmpty</code>, <code>canDetermineSize</code>,
 * <code>canDetermineContains</code> and <code>canDetermineElements</code>. If
 * one of these methods returns false, then the behavior of the corresponding
 * Set interface method(s) is undefined.
 * </ul>
 */
public interface ObjectSet extends Set {
	/**
	 * Returns an object sampled uniformly at random from this set, assuming that
	 * the given number <code>n<code> is sampled uniformly 
	 * from the numbers 0, ..., <i>size</i> - 1 (where <i>size</i> is the 
	 * size of this set).  
	 * 
	 * <p>If the underlying partial world is not complete enough to 
	 * fully define this set, this method returns null.
	 * 
	 * @throws IllegalArgumentException
	 *           if <code>n</code> is less than 0 or greater than or equal to the
	 *           size of this set
	 */
	Object sample(int n);

	/**
	 * Returns the number which, when passed to <code>sample</code>, will yield
	 * the given element of this set. If the given object is not in this set,
	 * returns -1.
	 * 
	 * <p>
	 * If the underlying partial world is not complete enough to fully define this
	 * set, this method returns -1.
	 */
	int indexOf(Object o);

	/**
	 * Returns an ObjectIterator whose <code>skipIndistinguishable</code> method
	 * treats all of the given objects as distinguishable from all other objects,
	 * even if they are indistinguishable in the underlying partial world. These
	 * <i>externally distinguishable</i> objects are typically the values of
	 * logical variables in the current scope -- the point is that even if the
	 * partial world makes the same assertions about two objects, these objects
	 * may satisfy different formulas if one is the value of a currently bound
	 * variable and one is not.
	 */
	ObjectIterator iterator(Set externallyDistinguished);

	/**
	 * Returns true if the underlying partial world is complete enough to
	 * determine whether this set is empty.
	 */
	boolean canDetermineIsEmpty();

	/**
	 * Returns true if the underlying partial world is complete enough to
	 * determine the size of this set.
	 */
	boolean canDetermineSize();

	/**
	 * Returns true if the underlying partial world is complete enough to
	 * determine whether the given object is in this set.
	 */
	boolean canDetermineContains(Object obj);

	/**
	 * Returns true if the underlying partial world is complete enough to
	 * determine all the elements of this set.
	 */
	boolean canDetermineElements();

	/**
	 * Returns a version of this set that no longer reflects changes to the
	 * partial world or evaluation context. It may still be backed by a world's
	 * POP application satisfier sets (so that new identifiers are added
	 * properly), but other than that, its methods will not call any methods on
	 * the underlying partial world.
	 * 
	 * <p>
	 * This method returns null if the underlying partial world is not complete
	 * enough to construct an explicit version of this set.
	 */
	ObjectSet getExplicitVersion();

	static final ObjectSet EMPTY_OBJECT_SET = new AbstractObjectSet() {
		protected Integer sizeInternal() {
			return new Integer(0);
		}

		protected Boolean containsInternal(Object o) {
			return Boolean.FALSE;
		}

		public ObjectIterator iterator(Set externallyDistinguished) {
			return new DefaultObjectIterator(Collections.EMPTY_SET.iterator());
		}

		public ObjectSet getExplicitVersion() {
			return this;
		}

		public Object sample(int n) {
			throw new IllegalArgumentException("Can't sample element " + n
					+ " from empty set.");
		}

		public int indexOf(Object o) {
			return -1;
		}
	};

	static final ObjectSet UNDETERMINED_SET = new AbstractObjectSet() {
		protected Integer sizeInternal() {
			return null;
		}

		protected Boolean containsInternal(Object o) {
			return null;
		}

		public ObjectSet getExplicitVersion() {
			return null;
		}

		public ObjectIterator iterator(Set externallyDistinguished) {
			return new ObjectIterator() {
				public boolean hasNext() {
					return false;
				}

				public Object next() {
					throw new NoSuchElementException("Can't get next element from "
							+ "undetermined set.");
				}

				public void remove() {
					throw new UnsupportedOperationException("Can't remove element from "
							+ "undetermined set.");
				}

				public boolean canDetermineNext() {
					return false;
				}

				public int skipIndistinguishable() {
					return 0;
				}
			};
		}

		public Object sample(int n) {
			return null;
		}

		public int indexOf(Object o) {
			return -1;
		}
	};
}
