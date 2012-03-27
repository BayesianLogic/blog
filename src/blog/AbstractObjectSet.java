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
 * Abstract implementation of the ObjectSet interface. It includes protected
 * methods <code>isEmptyInternal</code>, <code>sizeInternal</code>, and
 * <code>containsInternal</code> that return Boolean and Integer objects rather
 * than primitive data types. Null return values indicate that the corresponding
 * Set interface method should throw an IllegalStateException and the
 * corresponding <code>canDetermine</code> method should return false.
 */
public abstract class AbstractObjectSet extends AbstractSet implements
		ObjectSet {
	public boolean isEmpty() {
		Boolean isEmpty = isEmptyInternal();
		if (isEmpty == null) {
			throw new IllegalArgumentException(
					"Underlying partial world is not complete enough to "
							+ "determine if set is empty: " + this);
		}
		return isEmpty.booleanValue();
	}

	public boolean canDetermineIsEmpty() {
		return (isEmptyInternal() != null);
	}

	public int size() {
		Integer size = sizeInternal();
		if (size == null) {
			throw new IllegalArgumentException(
					"Underlying partial world is not complete enough to "
							+ "determine size of set: " + this);
		}
		return size.intValue();
	}

	public boolean canDetermineSize() {
		return (sizeInternal() != null);
	}

	public boolean contains(Object obj) {
		Boolean result = containsInternal(obj);
		if (result == null) {
			throw new IllegalArgumentException(
					"Underlying partial world is not complete enough to "
							+ "determine whether " + obj + " is in set: " + this);
		}
		return result.booleanValue();
	}

	public boolean canDetermineContains(Object obj) {
		return (containsInternal(obj) != null);
	}

	/**
	 * Returns the iterator obtained by calling
	 * <code>iterator(Collections.EMPTY_SET)</code>: that is, an iterator that
	 * recognizes no externally distinguished objects.
	 */
	public Iterator iterator() {
		return iterator(Collections.EMPTY_SET);
	}

	public boolean canDetermineElements() {
		return (getExplicitVersion() != null);
	}

	/**
	 * Returns Boolean.TRUE if this set is empty, Boolean.FALSE if it is not
	 * empty, and null if the underlying partial world is not complete enough to
	 * determine whether the set is empty.
	 * 
	 * <p>
	 * The default implementation just uses the result of sizeInternal.
	 */
	protected Boolean isEmptyInternal() {
		Integer n = sizeInternal();
		if (n == null) {
			return null;
		}
		return Boolean.valueOf(n.intValue() == 0);
	}

	/**
	 * Returns the size of this set, or null if the underlying partial world is
	 * not complete enough to determine the size.
	 */
	protected abstract Integer sizeInternal();

	/**
	 * Returns Boolean.TRUE if this set contains <code>obj</code>, Boolean.FALSE
	 * if this set does not contain <code>obj</code>, and null if the underlying
	 * partial world is not complete enough to make the distinction.
	 */
	protected abstract Boolean containsInternal(Object obj);

	public static ObjectSet singleton(Object element) {
		return new SingletonSet(element);
	}

	private static class SingletonSet extends AbstractObjectSet {
		SingletonSet(Object element) {
			this.element = element;
		}

		protected Integer sizeInternal() {
			return new Integer(1);
		}

		protected Boolean containsInternal(Object o) {
			return Boolean.valueOf(element.equals(o));
		}

		public ObjectSet getExplicitVersion() {
			return this;
		}

		public ObjectIterator iterator(Set externallyDistinguished) {
			return new DefaultObjectIterator(Collections.singleton(element)
					.iterator());
		}

		public Object sample(int n) {
			if (n == 0) {
				return element;
			}
			throw new IllegalArgumentException("Can't sample element " + n
					+ " from singleton set.");
		}

		public int indexOf(Object o) {
			if (element.equals(o)) {
				return 0;
			}
			return -1;
		}

		private Object element;
	}
}
