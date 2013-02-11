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
 * * Neither the name of the Massachusetts Institute of Technology nor
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

package ve;

import java.util.*;

/**
 * Represents a mapping from argument tuples in a "superset" multi-array to
 * corresponding argument tuples in a "subset" multi-array.
 */
public class DimMapping {

	// For each index i in the subset multi-array's dimension list,
	// p_reverse[i] is the corresponding index in the superset multi-array's
	// dimension list.
	private int[] p_reverse;

	// For each index j in the superset multi-array's dimension list,
	// p_forward[j] is the corresponding index in the subset multi-array's
	// dimension list, or THROW_OUT if the subset multi-array does not
	// include that dimension.
	private int[] p_forward = null;

	private final static int THROW_OUT = -1;

	public static void main(String[] args) {
		int[] p_test = { 5, 2, 3, 4 };
		DimMapping p = new DimMapping(p_test);
		int[] supposed_p_forward = { THROW_OUT, THROW_OUT, 1, 2, 3, 0 };
		int[] computed_p_forward = p.indicesInSub();
		int i;
		for (i = 0; i < supposed_p_forward.length; i++) {
			System.out.print(i);
			System.out.print(",");
			System.out.print(computed_p_forward[i]);
			System.out.print(",");
			System.out.print(supposed_p_forward[i]);
			System.out.println();

			if (supposed_p_forward[i] != computed_p_forward[i])
				break;
		}
		if (i != supposed_p_forward.length)
			System.out.println("p_inversion does not work!");
		else
			System.out.println("p_inversion works!");
	}

	public static DimMapping identityDimMapping(int length) {
		int[] p_reverse = new int[length];
		for (int i = 0; i < length; i++)
			p_reverse[i] = i;
		return new DimMapping(p_reverse);
	}

	public DimMapping(int[] p_reverse) {
		this.p_reverse = p_reverse;
	}

	public DimMapping(List<Integer> p_reverse) {
		ListIterator<Integer> perm_iter = p_reverse.listIterator();
		int[] array_perm = new int[p_reverse.size()];
		for (int i = 0; i < array_perm.length; i++)
			array_perm[i] = perm_iter.next();
		this.p_reverse = array_perm;
	}

	private void generatePForward() {
		int max = -1;
		for (int i = 0; i < p_reverse.length; i++)
			max = max > p_reverse[i] ? max : p_reverse[i];
		p_forward = new int[max + 1];

		Arrays.fill(p_forward, THROW_OUT);
		for (int i = 0; i < p_reverse.length; ++i) {
			p_forward[p_reverse[i]] = i;
		}
	}

	/**
	 * Returns an array <code>a</code> such that <code>a[i]</code> is the
	 * dimension in the superset multi-array that corresponds to dimension
	 * <code>i</code> in the subset multi-array. This array should not be
	 * modified.
	 */
	public int[] indicesInSuper() {
		return p_reverse;
	}

	/**
	 * Returns an array <code>a</code> such that <code>a[i]</code> is the
	 * dimension in the subset multi-array that corresponds to dimension i in the
	 * superset multi-array, or -1 if there is no corresponding dimension in in
	 * the subset multi-array. This array may be shorter than the number of
	 * dimensions in the superset multi-array, which implies that the remaining
	 * superset dimensions have no counterparts in the subset. The array returned
	 * should not be modified.
	 */
	public int[] indicesInSub() {
		if (p_forward == null) {
			generatePForward();
		}
		return p_forward;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("[sub indices in sup:");
		for (int i = 0; i < p_reverse.length; ++i) {
			buf.append(" ");
			buf.append(p_reverse[i]);
		}
		buf.append("]");
		return buf.toString();
	}
}
