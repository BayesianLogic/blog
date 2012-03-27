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
 * * Neither the name of the Massachusetts Institute of Technology, 
     nor the name of the University of California, Berkeley, nor
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
 * Simple implementation of a sparse vector, that is, a vector of doubles in
 * which many elements are zero. Only the non-zero elements are stored
 * explicitly. The class uses two arrays: one to hold the non-zero values, and
 * one to hold their indices. The indices (and corresponding values) are kept in
 * sorted order, so accessing an element by index takes time logarithmic in the
 * number of non-zero entries.
 */
public class SparseVector extends ImplicitVector {
	/**
	 * Creates a new vector with no non-zero entries.
	 */
	public SparseVector() {
	}

	public double get(int i) {
		int pos = getIndex(i);
		if (pos >= 0) {
			return values[pos];
		}
		return 0;
	}

	/**
	 * Sets the value at the given index. The value is stored explicitly only if
	 * it is non-zero.
	 */
	public void set(int i, double value) {
		int pos = getIndex(i);
		if (pos >= 0) {
			if (value == 0) {
				delete(pos);
			} else {
				values[pos] = value;
			}
		} else if (value != 0) {
			pos = -(pos + 1);
			open(pos);
			indices[pos] = i;
			values[pos] = value;
		}
	}

	public int numExplicit() {
		return numNonZero;
	}

	public int ithExplicitIndex(int i) {
		return indices[i];
	}

	public double ithExplicitValue(int i) {
		return values[i];
	}

	/**
	 * Returns the index of key in the first numNonZero elements of
	 * <code>indices</code>. Or, if key is not present, returns (-pos - 1), where
	 * pos is the position where key would be inserted into the sorted sequence.
	 */
	private int getIndex(int key) {
		// do binary search
		int lower = 0;
		int strictUpper = numNonZero;
		while (lower < strictUpper) {
			int i = lower + ((strictUpper - lower) / 2);
			if (indices[i] == key) {
				return i;
			}
			if (indices[i] < key) {
				lower = i + 1;
			} else {
				strictUpper = i;
			}
		}
		return -lower - 1;
	}

	private void delete(int pos) {
		System.arraycopy(indices, pos + 1, indices, pos, numNonZero - pos - 1);
		System.arraycopy(values, pos + 1, values, pos, numNonZero - pos - 1);
		--numNonZero;
	}

	private void open(int pos) {
		if (numNonZero == indices.length) {
			// double the allocated space
			int[] newIndices = new int[indices.length * 2];
			double[] newValues = new double[indices.length * 2];

			// copy up to pos
			System.arraycopy(indices, 0, newIndices, 0, pos);
			System.arraycopy(values, 0, newValues, 0, pos);

			// copy from pos onwards, leaving pos open in new arrays
			int len = numNonZero - pos;
			System.arraycopy(indices, pos, newIndices, pos + 1, len);
			System.arraycopy(values, pos, newValues, pos + 1, len);

			// change member variables
			indices = newIndices;
			values = newValues;
		} else {
			// copy from pos onwards, leaving pos open in new arrays
			int len = numNonZero - pos;
			System.arraycopy(indices, pos, indices, pos + 1, len);
			System.arraycopy(values, pos, values, pos + 1, len);
		}
		++numNonZero;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		for (int i = 0; i < numNonZero; ++i) {
			buf.append(indices[i]);
			buf.append(":");
			buf.append(values[i]);
			if (i + 1 < numNonZero) {
				buf.append(", ");
			}
		}
		buf.append("]");
		return buf.toString();
	}

	private static final int INITIAL_SIZE = 8;

	private int[] indices = new int[INITIAL_SIZE];
	private double[] values = new double[INITIAL_SIZE];
	int numNonZero = 0;

	/**
	 * Test program.
	 */
	public static void main(String[] args) {
		Util.initRandom(false);

		SparseVector v = new SparseVector();
		double[] arr = new double[1000];
		Arrays.fill(arr, 0);

		// Use shuffle to get 100 distinct indices less than 1000
		List indices = new ArrayList();
		for (int i = 0; i < 1000; ++i) {
			indices.add(new Integer(i));
		}
		Util.shuffle(indices);

		// Put random numbers at these indices
		System.out.println("Creating 100 non-zero entries...");
		for (int i = 0; i < 100; ++i) {
			int index = ((Integer) indices.get(i)).intValue();
			arr[index] = Util.random();
			v.set(index, arr[index]);
		}

		// Compare values in array and SparseVector
		System.out.println("Number of mismatches with dense array: "
				+ countMismatches(arr, v));

		// Set the first 20 elements in the shuffled sequence to zero
		System.out.println("Setting 20 entries to zero...");
		for (int i = 0; i < 20; ++i) {
			int index = ((Integer) indices.get(i)).intValue();
			arr[index] = 0;
			v.set(index, 0);
		}

		// Compare values in array and SparseVector
		System.out.println("Number of mismatches with dense array: "
				+ countMismatches(arr, v));

		System.out.println("Num non-zero: " + v.numExplicit());

		// Create another SparseVector and the equivalent array

		List otherIndices = new ArrayList();
		for (int i = 0; i < 1000; ++i) {
			otherIndices.add(new Integer(i));
		}
		Util.shuffle(otherIndices);

		SparseVector other = new SparseVector();
		double[] otherArr = new double[1000];
		Arrays.fill(otherArr, 0);

		for (int i = 0; i < 100; ++i) {
			int index = ((Integer) otherIndices.get(i)).intValue();
			otherArr[index] = Util.random();
			other.set(index, otherArr[index]);
		}

		// Compare sparse and dense dot products
		double dotProduct = v.dotProduct(other);
		System.out.println("Sparse dot product: " + dotProduct);
		double sum = 0;
		for (int i = 0; i < arr.length; ++i) {
			sum += arr[i] * otherArr[i];
		}
		System.out.println("Dense dot product: " + sum);
	}

	private static int countMismatches(double[] arr, SparseVector v) {
		int numMismatches = 0;
		for (int i = 0; i < arr.length; ++i) {
			if (arr[i] != v.get(i)) {
				++numMismatches;
			}
		}
		return numMismatches;
	}
}
