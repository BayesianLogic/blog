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
 * A data structure that is implemented as a linked list, but only implements
 * the Collection interface, so it does not allow random access to elements and
 * does not support ListIterators. However, the iterators used in an
 * ExtensibleLinkedList are very robust: the only thing that invalidates an
 * iterator is if some other piece of code (other than the iterator's
 * <code>remove</code> method) removes the last element returned by that
 * iterator's <code>next</code> method.
 */
public class ExtensibleLinkedList extends AbstractCollection {
	/**
	 * Creates an empty list.
	 */
	public ExtensibleLinkedList() {
	}

	/**
	 * Creates a list containing the elements of the specified collection, in the
	 * order they are returned by the collection's iterator.
	 */
	public ExtensibleLinkedList(Collection c) {
		addAll(c);
	}

	public int size() {
		return size;
	}

	public Iterator iterator() {
		return new RobustIterator();
	}

	/**
	 * Adds the given object to the end of this list.
	 */
	public boolean add(Object o) {
		if (tail == null) {
			// adding first element to list
			head = new Cell(o, null, null);
			tail = head;
		} else {
			tail.next = new Cell(o, tail, null);
			tail = tail.next;
		}
		++size;
		return true;
	}

	private class RobustIterator implements Iterator {
		public boolean hasNext() {
			if (predOfNext == null) {
				return (head != null);
			}

			if (predOfNext.data == REMOVED) {
				throw new ConcurrentModificationException(
						"Removed list cell that iterator was pointing to.");
			}

			return (predOfNext.next != null);
		}

		public Object next() {
			if (predOfNext == null) {
				if (head == null) {
					throw new NoSuchElementException();
				}
				predOfNext = head;
			} else {
				if (predOfNext.data == REMOVED) {
					throw new ConcurrentModificationException(
							"Removed list cell that iterator was pointing to.");
				}

				if (predOfNext.next == null) {
					throw new NoSuchElementException();
				}

				predOfNext = predOfNext.next;
			}

			removedLast = false;
			return predOfNext.data; // now that predOfNext has been updated
		}

		public void remove() {
			if (predOfNext == null) {
				throw new IllegalStateException("next has not been called.");
			}
			if (removedLast) {
				throw new IllegalStateException("Last item already removed.");
			}

			if (predOfNext.data == REMOVED) {
				throw new ConcurrentModificationException(
						"Removed list cell that iterator was pointing to.");
			}

			predOfNext.data = REMOVED;

			if (predOfNext.next == null) {
				tail = predOfNext.prev; // removing last element
			} else {
				predOfNext.next.prev = predOfNext.prev;
			}

			if (predOfNext.prev == null) {
				head = predOfNext.next; // removing first element
				predOfNext = null;
			} else {
				predOfNext.prev.next = predOfNext.next;
				predOfNext = predOfNext.prev;
			}

			removedLast = true;
			--size;
		}

		private Cell predOfNext = null; // null only at beginning
		private boolean removedLast = false;
	}

	private class Cell {
		Cell(Object data, Cell prev, Cell next) {
			this.data = data;
			this.prev = prev;
			this.next = next;
		}

		Object data;
		Cell prev;
		Cell next;
	}

	private int size = 0;
	private Cell head = null;
	private Cell tail = null;

	private static final Integer REMOVED = new Integer(0);
}
