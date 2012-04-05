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
 * Class for iterating over tuples that are added to the Cartesian product A_1 x
 * A_2 x ... x A_n when the original collections A_1, ..., A_n are augmented
 * with additional collections B_1, ..., B_n. In mathematical notation (with "u"
 * for the union symbol), the iteration is over: <blockquote> ((A_1 u B_1) x ...
 * x (A_n u B_n)) - (A_1 x ... x A_n) </blockquote> We assume that A_i and B_i
 * are disjoint.
 * 
 * <p>
 * The trick is to partition the "added" tuples according to the index where we
 * first encounter an added element. The set of tuples where the first added
 * element occurs at index i is the Cartesian product: <blockquote> A_1 x ... x
 * A_{i-1} x B_i x (A_{i+1} u B_{i+1}) x ... x (A_n u B_n) </blockquote> So
 * overall, we just need to iterate over the (disjoint) union of such Cartesian
 * products for i = 1 to n.
 */
public class AddedTupleIterator implements Iterator {
	/**
	 * Creates a new iterator over tuples that are added to the Cartesian product
	 * when the collections in <code>orig</code> are augmented with the
	 * corresponding collections in <code>added</code>.
	 * 
	 * @param orig
	 *          List of Collection
	 * @param added
	 *          List of Collection
	 * 
	 * @throws IllegalArgumentException
	 *           if <code>orig</code> and <code>added</code> have different sizes
	 */
	public AddedTupleIterator(List orig, List added) {
		if (orig.size() != added.size()) {
			throw new IllegalArgumentException(
					"Original collections must be in one-to-one correspondence "
							+ "with added collections.");
		}

		this.orig = orig;
		this.added = added;

		loadNext();
	}

	public boolean hasNext() {
		return (nextTuple != null);
	}

	public Object next() {
		if (nextTuple == null) {
			throw new NoSuchElementException();
		}

		Object toReturn = nextTuple;
		loadNext();
		return toReturn;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	private void loadNext() {
		nextTuple = null;

		while ((curCartProductIter == null) || !curCartProductIter.hasNext()) {
			// Need a new Cartesian product to iterate over
			// Increase indexOfFirstAdded and see if we get a non-empty one

			if (indexOfFirstAdded + 1 >= orig.size()) {
				return; // can't increment any more
			}
			++indexOfFirstAdded;

			if (((Collection) added.get(indexOfFirstAdded)).isEmpty()) {
				// Don't bother constructing empty Cartesian product
				continue;
			}

			// Set up the Cartesian product that consists of
			// tuples in which the first added element occurs at
			// indexOfFirstAdded.
			List factors = new ArrayList();
			for (int i = 0; i < indexOfFirstAdded; ++i) {
				factors.add(orig.get(i));
			}
			factors.add(added.get(indexOfFirstAdded));
			for (int i = indexOfFirstAdded + 1; i < orig.size(); ++i) {
				factors.add(Util.disjointUnion((Collection) orig.get(i),
						(Collection) added.get(i)));
			}

			curCartProductIter = new TupleIterator(factors);
		}

		// If we get here, then curCartProductIter has a next element
		nextTuple = (List) curCartProductIter.next();
	}

	private List orig; // of Collection
	private List added; // of Collection

	private int indexOfFirstAdded = -1;
	private Iterator curCartProductIter = null;
	private List nextTuple = null;
}
