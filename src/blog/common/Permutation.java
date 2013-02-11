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
 * Class with static methods for dealing with permutations. A permutation pi of
 * the integers 0,...,n-1 is represented as an array of length n, with the ith
 * element of the array being pi(i).
 * 
 * Several of the methods have to do with <i>inversions</i>: an inversion of a
 * permutation pi is a pair (i,j) such that i &lt; j but pi(i) &gt; pi(j). The
 * number of inversions of a permutation is the minimum number of transpositions
 * required to transform the sorted sequence 1,2,...,n into this permutation.
 */
class Permutation {
	/**
	 * Returns true if pi is a permutation, that is, each number from 0 through
	 * pi.length - 1 occurs exactly once in pi. This requires that no numbers
	 * outside the set {0,...,pi.length-1} occur in pi.
	 */
	public static boolean isPermutation(int[] pi) {
		boolean[] seen = new boolean[pi.length];

		for (int i = 0; i < pi.length; i++) {
			if ((pi[i] < 0) || (pi[i] >= pi.length)) {
				return false;
			}
			if (seen[pi[i]]) {
				return false;
			}
			seen[pi[i]] = true;
		}

		// Array contains no numbers outside {0,...,pi.length-1}, and doesn't
		// contain same number twice, so must contain all numbers in that set.
		return true;
	}

	/**
	 * Given an array of integers, returns the permutation that would have to be
	 * performed on the sorted version of this array to obtain the given array.
	 */
	public static int[] toPermutation(int[] arr) {
		SortedMap numsToPositions = new TreeMap();
		for (int i = 0; i < arr.length; i++) {
			numsToPositions.put(new Integer(arr[i]), new Integer(i));
		}

		// Iterate over map entries in sorted order by key. For each i,
		// record what position the ith key had in arr.
		int[] perm = new int[arr.length];
		int i = 0;
		for (Iterator iter = numsToPositions.values().iterator(); iter.hasNext();) {
			perm[i++] = ((Integer) iter.next()).intValue();
		}

		return perm;
	}

	/**
	 * Returns the maximum number of inversions that a permutation of n elements
	 * can have. This is equal to (n choose 2), and is achieved by the permutation
	 * n-1,n-2,...,1,0.
	 */
	public static int maxInversions(int n) {
		return (n * (n - 1)) / 2;
	}

	/**
	 * Returns the number of inversions of the given permutation. This
	 * implementation is quadratic in pi.length. There is an exercise in volume 3
	 * of Knuth asking for an O(n log n) algorithm, but n will be small (the
	 * number of authors on a paper) for us, so I didn't bother trying to work out
	 * that faster algorithm.
	 */
	public static int numInversions(int[] pi) {
		if (!isPermutation(pi)) {
			throw new IllegalArgumentException("Not a permutation");
		}

		int total = 0;
		for (int i = 0; i < pi.length - 1; i++) {
			// Increment total for each j > i such that pi(j) < pi(i)
			for (int j = i + 1; j < pi.length; j++) {
				if (pi[j] < pi[i]) {
					total++;
				}
			}
		}

		return total;
	}

	/**
	 * Returns the number of permutations of a set of size n that have k
	 * inversions. See p. 15 of Knuth volume 3.
	 */
	public static int numWithKInversions(int n, int k) {
		for (int i = inversionInfo.size(); i <= n; i++) {
			addInversionInfo(i);
		}

		return ((InversionInfo) inversionInfo.get(n)).numPermsWithK(k);
	}

	static class InversionInfo {
		public InversionInfo(int n) {
			maxInversions = Permutation.maxInversions(n);
			numPermsWithK = new int[(maxInversions / 2) + 1]; // +1 for zero
		}

		public int numPermsWithK(int k) {
			if ((k < 0) || (k > maxInversions)) {
				return 0;
			}

			// The number of permutations with k inversions is equal to the
			// number with (maxInversions - k) inversions.
			return numPermsWithK[Math.min(k, maxInversions - k)];
		}

		int maxInversions;
		int[] numPermsWithK;
	}

	static void addInversionInfo(int n) {
		InversionInfo info = new InversionInfo(n);
		for (int k = 0; k < info.numPermsWithK.length; k++) {

			// How many permutations of n elements have k inversions?
			// Well, if n=1, the answer is 1 if k=0 and 0 otherwise.
			// For n > 1, there's a one-to-one correspondence between
			// permutations pi of n elements and pairs (r, pi'), where
			// r = pi(0) and pi' is the permutation that pi imposes on
			// 1,...,n-1. Note that any permutation pi has pi(0) = r
			// inversions involving 0. So in order to have k
			// inversions total, it must have k-r inversions not
			// involving 0. So the number of n-permutations pi with k
			// inversions is the sum, over all possible values of r,
			// of the number of (n-1)-permutations pi' with k-r
			// inversions.
			if (n == 0) {
				info.numPermsWithK[k] = 0;
			} else if (n == 1) {
				// k must equal 0 because maxInversions(1) is 0
				info.numPermsWithK[k] = 1;
			} else {
				info.numPermsWithK[k] = 0;
				int rMin = Math.max(0, k - maxInversions(n - 1));
				int rMax = Math.min(n - 1, k);
				for (int r = rMin; r <= rMax; r++) {
					info.numPermsWithK[k] += numWithKInversions(n - 1, k - r);
				}
			}
		}
		inversionInfo.add(info);
	}

	/**
	 * List whose nth element is an InversionInfo object storing inversion
	 * information for permutations of length n. We create objects for greater and
	 * greater values of n as needed.
	 */
	static List inversionInfo = new ArrayList();

	public static void main(String[] args) {
		for (int n = 1; n <= 10; n++) {
			System.out.print(n + ":");
			for (int k = 0; k <= Permutation.maxInversions(n); k++) {
				int result = Permutation.numWithKInversions(n, k);
				System.out.print(" " + result);
			}
			System.out.println("");
		}
	}
}
