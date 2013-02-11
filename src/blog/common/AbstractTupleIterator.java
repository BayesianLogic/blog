/*
 * Copyright (c) 2005, 2006, Regents of the University of California
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
 * An iterator over the set of tuples <i>(x1, ..., xk)</i> such that <i>x1</i>
 * is in <i>S1</i>, <i>x2</i> is in <i>S2(x1)</i>, etc., and <i>xk</i> is in
 * <i>Sk(x1, ..., x(k-1))</i>. This is a generalization of the standard
 * TupleIterator, where <i>Si</i> does not depend on <i>x1, ..., x(i-1)</i>.
 * Subclasses should override the <code>getIterator</code> method.
 * 
 * <p>
 * The algorithm is depth-first search on a tree where edges are labeled with
 * tuple elements. If a node is reached by the path <i>x1, ..., x(i-1)</i>, then
 * its children are labeled with the elements of <i>Si(x1, ..., x(i-1))</i>. A
 * node at depth <i>k</i> corresponds to a complete tuple. If some of the sets
 * <i>Si</i> are empty, then some nodes at depths less than <i>k</i> are dead
 * ends with no children. The algorithm backtracks when it reaches one of these
 * nodes.
 */
public abstract class AbstractTupleIterator implements Iterator {
	/**
	 * Creates a new AbstractTupleIterator for tuples of length <code>k</code>.
	 */
	public AbstractTupleIterator(int k) {
		curState = new Iterator[k];
		nextTuple = new ArrayList(Collections.nCopies(k, null));
	}

	/**
	 * Returns true if there are any more tuples to return.
	 */
	public boolean hasNext() {
		if (!initialized) {
			// so subclass constructor doesn't need to call initialize
			initialize();
		}
		return (nextTuple != null);
	}

	/**
	 * Returns another tuple not returned previously.
	 */
	public Object next() {
		if (!initialized) {
			// so subclass constructor doesn't need to call initialize
			initialize();
		}

		if (nextTuple == null) {
			throw new NoSuchElementException();
		}

		// It's important that we return a new list, not the list that we'll
		// be modifying to create the next tuple (namely nextTuple). The
		// caller might be storing some of these tuples in a collection,
		// which would yield unexpected results if we just gave them the
		// same List object over and over.
		List result = new ArrayList(nextTuple);
		loadNext();
		return result;
	}

	private void initialize() {
		depth = 0;
		if (curState.length > 0) {
			curState[depth] = getIterator(depth, nextTuple);
			loadNext();
		}
		initialized = true;
	}

	/**
	 * Continue DFS from the current node until we reach another node that
	 * represents a complete tuple (i.e., has depth k). Set nextTuple equal to the
	 * resulting tuple, or null if there are no more tuples to enumerate.
	 */
	private void loadNext() {
		while (depth >= 0) {
			if (curState[depth].hasNext()) {
				nextTuple.set(depth, curState[depth].next());

				if (depth == curState.length - 1) {
					break; // got complete tuple
				} else {
					// go deeper in tree
					++depth;
					curState[depth] = getIterator(depth, nextTuple);
				}
			} else {
				doneWithIterator(curState[depth]);
				--depth; // pop the stack
			}
		}

		// If we found a valid tuple, depth is now curState.length - 1.
		// Otherwise, we have exhausted all the objects from the first
		// iterator, and depth has been decremented to -1.

		if (depth < 0) {
			nextTuple = null;
		}
	}

	/** OPTIONAL METHOD -- NOT IMPLEMENTED. */
	public void remove() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns an iterator over the <i>i</i>th set given that <i>x1, ...,
	 * x(i-1)</i> have the values specified in the given tuple. The entries in the
	 * tuple from index <i>i</i> onwards should be ignored.
	 * 
	 * @param indexInTuple
	 *          the index <i>i</i>
	 */
	protected abstract Iterator getIterator(int indexInTuple, List tuple);

	/**
	 * Method called whenever the AbstractTupleIterator is done with an iterator
	 * returned earlier by <code>getIterator</code> (i.e., that iterator's
	 * <code>hasNext</code> method has returned false). The default implementation
	 * does nothing, but subclasses can override it.
	 */
	protected void doneWithIterator(Iterator iter) {
	}

	private boolean initialized = false;

	private Iterator[] curState;
	private List nextTuple = new ArrayList();
	private int depth;
}
