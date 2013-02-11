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

import java.util.Iterator;
import java.util.BitSet;
import java.util.NoSuchElementException;

/**
 * An iterator for iterating over all the ways to make an ordered list of k
 * distinct elements from a set of size n. When k = n, this means iterating over
 * all the permutations of a set of size n.
 * 
 * We assume the set of size n can be ordered in some way, so that its elements
 * are numbered 0, 1, ..., n-1. A permutation is represented as an integer array
 * of length k containing numbers in {0, 1, ..., n-1}. Each number occurs at
 * most once in the permutation.
 */
public class PermutationIterator implements Iterator {
	/**
	 * Creates a new PermutationIterator for iterating over permutations of k
	 * elements from a set of size n.
	 */
	public PermutationIterator(int n, int k) {
		if ((n < 0) || (k < 0)) {
			throw new IllegalArgumentException("Negative argument to "
					+ "PermutationIterator");
		}
		if (k > n) {
			throw new IllegalArgumentException("Tried to iterate over "
					+ "permutations of length " + k + " from set of size " + n);
		}

		this.n = n;
		this.k = k;
		remaining = new BitSet[k];
		for (int i = 0; i < k; i++) {
			remaining[i] = new BitSet(n);
		}
	}

	/**
	 * Returns true if there is a permutation that hasn't been returned yet.
	 */
	public boolean hasNext() {
		if (perm == null) {
			return true; // next() has not been called yet
		}

		for (int i = 0; i < k; i++) {
			if (remaining[i].nextSetBit(perm[i] + 1) != -1) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the next permutation. Note that this modifies the int[] that was
	 * returned by the previous call to <code>next</code>.
	 * 
	 * @return an int[] representing a permutation that has not already been
	 *         returned
	 */
	public Object next() {
		if (perm == null) {
			// First call to next(). We start with the permutation
			// 0, 1, ..., k.
			perm = new int[k];
			for (int i = 0; i < k; i++) {
				perm[i] = i;
				remaining[i].clear();
				remaining[i].set(i, n);
			}
			return perm;
		}

		// Find the last position where we have a remaining value that we
		// haven't used yet.
		for (int i = k - 1; i >= 0; i--) {
			int nextVal = remaining[i].nextSetBit(perm[i] + 1);
			if (nextVal == -1) {
				// No more possibilities for this position. Continue to
				// previous position.
				continue;
			}

			// Set new value at this position
			perm[i] = nextVal;

			// Update remaining sets and set new values at all later positions
			for (int j = i + 1; j < k; j++) {
				// Remaining set here is equal to remaining set at previous
				// position, minus value at previous position.
				remaining[j].clear();
				remaining[j].or(remaining[j - 1]);
				remaining[j].clear(perm[j - 1]);

				// Restart with first remaining value
				perm[j] = remaining[j].nextSetBit(0);
			}

			return perm;
		}

		// If we get here, there are no permutations left.
		throw new NoSuchElementException();
	}

	/**
	 * Not supported.
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}

	int n;
	int k;

	/**
	 * Array of length k storing the last permutation returned. Initially set to
	 * null.
	 */
	int[] perm;

	/**
	 * remaining[i] stores the set of numbers in {0, ..., n-1} that are not used
	 * before position i in perm. So remaining[0] stores all the numbers in {0,
	 * ..., n-1}, and remaining[k-1] stores n-k-1 numbers.
	 */
	BitSet[] remaining;

	public static void main(String[] args) {
		int n = Integer.parseInt(args[0]);
		int k = Integer.parseInt(args[1]);

		for (Iterator iter = new PermutationIterator(n, k); iter.hasNext();) {
			int[] perm = (int[]) iter.next();
			for (int i = 0; i < perm.length; i++) {
				System.out.print(perm[i] + " ");
			}
			System.out.println("");
		}
	}
}
