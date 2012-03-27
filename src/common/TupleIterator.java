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
 * This class is an iterator over all tuples in a finite cartesian product of
 * finite sets. Some special cases: if the number of sets passed in is zero,
 * then the Cartesian product contains one element, the empty tuple. If an empty
 * set is passed in, then the Cartesian product is empty.
 */
public class TupleIterator implements Iterator {

	/**
	 * Constructor.
	 * 
	 * @param cartproduct
	 *          a List of Collections from which the tuples' components are to be
	 *          draw.
	 */
	public TupleIterator(List cartproduct) {
		this.cartproduct = cartproduct;

		for (int i = 0; i < cartproduct.size(); i++) {
			Iterator curIter = ((Collection) cartproduct.get(i)).iterator();
			currstate.add(curIter);
			if (curIter.hasNext()) {
				nextelt.add(curIter.next());
			} else {
				nextelt = null;
				break;
			}
		}
	}

	/**
	 * Returns true if there are any more elements in the Cartesian product to
	 * return.
	 */
	public boolean hasNext() {
		return (nextelt != null);
	}

	/**
	 * Returns another tuple not returned previously.
	 * 
	 * <p>
	 * The iterator stores its state in the private member <code>currstate</code>
	 * -- an ArrayList of the iterators of the individual Collections in the
	 * cartesian product. In the start state, each iterator returns a single
	 * element. Afterwards, while iterator #1 has anything to return, we replace
	 * the first element of the previous tuple to obtain a new tuple. Once
	 * iterator #1 runs out of elements we replace it and advance iterator #2. We
	 * keep on advancing iterator #1 until it runs out of elements for the second
	 * time, reinitialize it again, and advance iterator #2 once more. We repeat
	 * these operations until iterator #2 runs out of elements and we start
	 * advancing iterator #3, and so on, until all iterators run out of elements.
	 * 
	 * <p>
	 * This method of generating the m-tuples is very similar to producing all
	 * numbers of m "digits" in the order of their magnitude, where the i-th
	 * "digit" is in base<sub>i</sub> = #(i-th Collection in the cartesian
	 * product) and we assume that the "numbers" in the i-th Collection are
	 * ordered in some way.
	 */
	public Object next() {
		if (nextelt == null) {
			throw new NoSuchElementException();
		}

		// It's important that we return a new list, not the list that we'll
		// be modifying to create the next tuple (namely nextelt). The
		// caller might be storing some of these tuples in a collection,
		// which would yield unexpected results if we just gave them the
		// same List object over and over.
		List result = new ArrayList(nextelt);

		// compute the next element
		boolean gotNext = false;
		for (int i = currstate.size() - 1; i >= 0; --i) {
			Iterator curIter = (Iterator) currstate.get(i);
			if (curIter.hasNext()) {
				// advance this iterator, we have next tuple
				nextelt.set(i, curIter.next());
				gotNext = true;
				break;
			} else {
				// reset this iterator to its beginning, continue loop
				curIter = ((Collection) cartproduct.get(i)).iterator();
				currstate.set(i, curIter);
				nextelt.set(i, curIter.next());
			}
		}
		if (!gotNext) {
			nextelt = null;
		}

		return result;
	}

	/** OPTIONAL METHOD -- NOT IMPLEMENTED. */
	public void remove() {
		throw new UnsupportedOperationException();
	}

	/** A List of Collections. */
	private List cartproduct;

	/** A List of iterators storing the tuple-iterator's state. */
	private List currstate = new ArrayList();

	private List nextelt = new ArrayList();

	/**
	 * Test program. The program takes any number of command line arguments, each
	 * of which represents a collection. A collection is represented as a
	 * whitespace-separated list of elements (so it must be put in quotes, or else
	 * it will be broken into separate command line arguments by the shell). The
	 * program should print out all the tuples that can be formed from these
	 * collections. For example: <blockquote> <code>
	 * &gt; java -cp . common.TupleIterator "a b" "c d"   <br>
	 * [a, c]      <br>
	 * [a, d]      <br>
	 * [b, c]      <br>
	 * [b, d]
	 * </code> </blockquote>
	 */
	public static void main(String[] args) {
		List collections = new ArrayList();
		for (int i = 0; i < args.length; ++i) {
			List curColl = new ArrayList();
			for (StringTokenizer tok = new StringTokenizer(args[i]); tok
					.hasMoreTokens();) {
				curColl.add(tok.nextToken());
			}
			collections.add(curColl);
		}

		for (Iterator iter = new TupleIterator(collections); iter.hasNext();) {
			System.out.println(iter.next());
		}
	}
}
