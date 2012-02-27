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
 * Class for iterating over all subsets of an ordered set.  The subsets 
 * are represented by BitSet objects; in fact, the same BitSet object is 
 * used to represent each subset in turn.  Of course, using this class is 
 * only practical for iterating over the subsets of a small set, since the 
 * number of subsets of a set of size n is 2^n.
 */
public class SubsetIterator implements Iterator {
    /**
     * Creates a new SubsetIterator over a set of size n.
     */
    public SubsetIterator(int n) {
	this.n = n;
	done = false;
    }

    /**
     * Returns true if the set of size n has a subset that hasn't been 
     * returned yet.
     */
    public boolean hasNext() {
	return !done;
    }

    /**
     * Returns a BitSet representing a subset that hasn't been returned yet.
     * Note that this modifies the BitSet object that was returned by the 
     * previous call to <code>next</code>, so you shouldn't rely on that 
     * object remaining stable.  
     */
    public Object next() {
	if (done) {
	    throw new NoSuchElementException();
	}

	if (s == null) {
	    s = new BitSet(n); // first call to next();
	} else {
	    // Treat set as number and add 1 to it.
	    int firstZero = s.nextClearBit(0);
	    s.set(firstZero);
	    s.clear(0, firstZero);
	}
		
	done = (s.cardinality() == n);
	return s;
    }

    /**
     * Throws an exception -- removal makes no sense for this iterator.
     */
    public void remove() {
	throw new UnsupportedOperationException();
    }

    int n;
    BitSet s;
    boolean done;

    public static void main(String args[]) {
	int size = Integer.parseInt(args[0]);

	for (Iterator iter = new SubsetIterator(size); iter.hasNext(); ) {
	    BitSet s = (BitSet) iter.next();

	    System.out.print("{");
	    boolean first = true;
	    for (int i = 0; i < size; i++) {
		if (s.get(i)) {
		    if (!first) {
			System.out.print(", ");
		    }
		    System.out.print(i);
		    first = false;
		}
	    }
	    System.out.println("}");
	}
    }
}
