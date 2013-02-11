/*
 * Copyright (c) 2007 Massachusetts Institute of Technology
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
 * An implicit representation of the Cartesian product of a list of lists. The
 * tuples in the Cartesian product are ordered lexicographically. When you
 * iterate over tuples, the last element changes fastest and the first element
 * changes slowest.
 * 
 * <p>
 * Unlike TupleIterator, this class supports accessing arbitrary tuples by their
 * index (with the get method), and computing the index for a given tuple (with
 * indexOf).
 */
public class CartesianProduct extends AbstractList<List<?>> {
	/**
	 * Creates an implicit representation of the Cartesian product of the given
	 * lists.
	 */
	public CartesianProduct(List<? extends List<?>> dimList) {
		dims = dimList.toArray(new List[0]);

		indexMultipliers = new int[dims.length];
		int multiplier = 1;
		for (int i = dims.length - 1; i >= 0; --i) {
			indexMultipliers[i] = multiplier;
			multiplier *= dims[i].size();
		}
		size = multiplier;
	}

	public int size() {
		return size;
	}

	public Iterator<List<?>> iterator() {
		return new ProductListIterator();
	}

	public ListIterator<List<?>> listIterator() {
		return new ProductListIterator();
	}

	public ListIterator<List<?>> listIterator(int index) {
		if ((index < 0) || (index > size)) {
			throw new IndexOutOfBoundsException(String.valueOf(index));
		}
		return new ProductListIterator(index);
	}

	public List<?> get(int index) {
		if ((index < 0) || (index >= size)) {
			throw new IndexOutOfBoundsException(String.valueOf(index));
		}

		List<Object> tuple = new ArrayList<Object>(dims.length);
		for (int i = 0; i < dims.length; ++i) {
			int thisDimIndex = index / indexMultipliers[i];
			tuple.add(dims[i].get(thisDimIndex));
			index %= indexMultipliers[i];
		}
		return tuple;
	}

	public int indexOf(Object o) {
		if (o instanceof List) {
			List<Object> tuple = (List<Object>) o;
			if (tuple.size() == dims.length) {
				int index = 0;
				for (int i = 0; i < dims.length; ++i) {
					int thisDimIndex = dims[i].indexOf(tuple.get(i));
					if (thisDimIndex == -1) {
						return -1;
					}
					index += (thisDimIndex * indexMultipliers[i]);
				}
				return index;
			}
		}
		return -1;
	}

	private class ProductListIterator implements ListIterator<List<?>> {

		ProductListIterator() {
			for (int i = 0; i < dims.length; ++i) {
				listIters[i] = dims[i].listIterator();
				if ((i < dims.length - 1) && listIters[i].hasNext()) {
					latestTuple[i] = listIters[i].next();
				}
			}

			// Don't initialize last element of latestTuple yet; that
			// will happen on the first call to next or previous.
		}

		/**
		 * Assumes index >= 0 and index <= size.
		 */
		ProductListIterator(int index) {
			nextIndex = index;

			if (nextIndex == size) {
				// Make every listIter point to the end of its list
				for (int i = 0; i < dims.length; ++i) {
					listIters[i] = dims[i].listIterator(dims[i].size());
					if ((i < dims.length - 1) && listIters[i].hasPrevious()) {
						latestTuple[i] = listIters[i].previous();
						listIters[i].next();
					}
				}
			} else {
				for (int i = 0; i < dims.length; ++i) {
					int thisDimIndex = index / indexMultipliers[i];
					listIters[i] = dims[i].listIterator(thisDimIndex);
					if ((i < dims.length - 1) && listIters[i].hasNext()) {
						latestTuple[i] = listIters[i].next();
					}
					index %= indexMultipliers[i];
				}
			}
			// Don't initialize last element of latestTuple yet; that
			// will happen on the first call to next or previous.
		}

		public boolean hasNext() {
			return (nextIndex < size);
		}

		public List<?> next() {
			if (nextIndex >= size) {
				throw new NoSuchElementException();
			}

			// Increment listIters, putting next elements in latestTuple
			boolean done = (dims.length == 0);
			for (int i = dims.length - 1; !done; --i) {
				if (listIters[i].hasNext()) {
					done = true;
				} else {
					// restart iterator for this dimension
					listIters[i] = dims[i].listIterator();
				}
				latestTuple[i] = listIters[i].next();
			}

			++nextIndex;
			return new ArrayList<Object>(Arrays.asList(latestTuple));
		}

		public boolean hasPrevious() {
			return (nextIndex > 0);
		}

		public List<?> previous() {
			if (nextIndex == 0) {
				throw new NoSuchElementException();
			}

			// Decrement listIters, putting previous elements in latestTuple
			boolean done = (dims.length == 0);
			for (int i = dims.length - 1; !done; --i) {
				if (i < dims.length - 1) {
					// listIter[i] is after the current dim-i value.
					// Move it so it's before that value.
					listIters[i].previous();
				}

				if (listIters[i].hasPrevious()) {
					done = true;
				} else {
					// restart iterator for this dimension
					listIters[i] = dims[i].listIterator(dims[i].size());
				}
				latestTuple[i] = listIters[i].previous();

				if (i < dims.length - 1) {
					// Move listIters[i] forward to maintain the
					// invariant that it's after the current value.
					listIters[i].next();
				}
			}

			--nextIndex;
			return new ArrayList<Object>(Arrays.asList(latestTuple));
		}

		public int nextIndex() {
			return nextIndex;
		}

		public int previousIndex() {
			return nextIndex - 1;
		}

		public void add(List<?> e) {
			throw new UnsupportedOperationException();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

		public void set(List<?> e) {
			throw new UnsupportedOperationException();
		}

		int nextIndex = 0;

		// listIter cursor position is *after* the current value for
		// all dimensions except the last one, where it's before the
		// current value.
		ListIterator<?>[] listIters = new ListIterator[dims.length];

		Object[] latestTuple = new Object[dims.length];
	}

	protected List[] dims;
	protected int[] indexMultipliers;
	protected int size;

	public static void main(String[] args) {
		Integer[] arr1 = { 0, 1, 2 };
		Integer[] arr2 = { 0, 1, 2, 3, 4, 5 };
		Integer[] arr3 = { 0, 1 };

		List<List<Integer>> lists = new ArrayList<List<Integer>>();
		lists.add(Arrays.asList(arr1));
		lists.add(Arrays.asList(arr2));
		lists.add(Arrays.asList(arr3));

		CartesianProduct prod = new CartesianProduct(lists);

		System.out.println("Iterating forwards, printing computed indices...");
		int index = 0;
		for (Iterator<List<?>> iter = prod.iterator(); iter.hasNext();) {
			System.out.print(index++);

			System.out.print('\t');
			List<?> tuple = iter.next();
			System.out.print(tuple);

			System.out.print('\t');
			int compIndex = prod.indexOf(tuple);
			System.out.println(compIndex);
		}

		System.out.println();
		System.out.println("Getting tuples by index...");
		for (int i = 0; i < prod.size(); ++i) {
			System.out.print(i);
			System.out.print('\t');
			System.out.println(prod.get(i));
		}

		System.out.println();
		System.out.println("Iterating backwards, with computed indices...");
		index = prod.size() - 1;
		for (ListIterator<List<?>> iter = prod.listIterator(prod.size()); iter
				.hasPrevious();) {
			System.out.print(index--);

			System.out.print('\t');
			List<?> tuple = iter.previous();
			System.out.print(tuple);

			System.out.print('\t');
			System.out.println(prod.indexOf(tuple));
		}

		System.out.println();
		System.out.println("Iterating forwards from index 11...");
		index = 11;
		for (ListIterator<List<?>> iter = prod.listIterator(11); iter.hasNext();) {
			System.out.print(index++);

			System.out.print('\t');
			List<?> tuple = iter.next();
			System.out.print(tuple);

			System.out.print('\t');
			System.out.println(prod.indexOf(tuple));
		}

		System.out.println();
		System.out.println("Iterating backwards from before index 11...");
		index = 10;
		for (ListIterator<List<?>> iter = prod.listIterator(11); iter.hasPrevious();) {
			System.out.print(index--);

			System.out.print('\t');
			List<?> tuple = iter.previous();
			System.out.print(tuple);

			System.out.print('\t');
			System.out.println(prod.indexOf(tuple));
		}

		lists.set(1, Collections.EMPTY_LIST);
		prod = new CartesianProduct(lists);

		System.out.println();
		System.out.println("Iterating forwards over empty product...");
		for (Iterator iter = prod.iterator(); iter.hasNext();) {
			System.out.println(iter.next());
		}
		System.out.println("done");

		System.out.println();
		System.out.println("Iterating backwards over empty product...");
		for (ListIterator iter = prod.listIterator(prod.size()); iter.hasPrevious();) {
			System.out.println(iter.previous());
		}
		System.out.println("done");

		lists.clear();
		lists.add(Arrays.asList(arr2));
		prod = new CartesianProduct(lists);

		System.out.println();
		System.out.println("Iterating forwards over 1-dim product...");
		for (Iterator iter = prod.iterator(); iter.hasNext();) {
			System.out.println(iter.next());
		}
	}
}
