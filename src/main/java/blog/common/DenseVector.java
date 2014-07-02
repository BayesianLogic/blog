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

package blog.common;

import java.util.*;

/**
 * Implementation of ImplicitVector in which the explicit entries form a prefix
 * of the implicit infinite vector. In other words, this is an ordinary vector
 * in which entries beyond the end of the explicitly represented vector are
 * treated as zeroes. Entries between the explicitly set entries that have not
 * been set themselves are also treated as zeroes.
 */
public class DenseVector extends ImplicitVector {
	/**
	 * Creates a new vector with no explicit entries.
	 */
	public DenseVector() {
		values = new double[INITIAL_SIZE];
	}

	/**
	 * Creates a new vector with no explicit entries, but with memory allocated
	 * for the given number of entries. This just affects memory management.
	 */
	public DenseVector(int initialCapacity) {
		values = new double[initialCapacity];
	}

	public double get(int i) {
		if (i < numExplicit) {
			return values[i];
		}
		return 0;
	}

	/**
	 * Sets the value at the given index, filling in any gap between this index
	 * and the previous highest explicit index with explicit zeroes. The specified
	 * value is stored explicitly even if it is zero.
	 */
	public void set(int i, double value) {
		ensureIndexReady(i);
		values[i] = value;
		if (numExplicit <= i) {
			for (int j = numExplicit; j < i; ++j) {
				values[j] = 0;
			}
			numExplicit = i + 1;
		}
	}

	/**
	 * Adds the given value at the next index after the last explicitly set index.
	 */
	public void add(double value) {
		ensureIndexReady(numExplicit);
		values[numExplicit] = value;
		++numExplicit;
	}

	public int numExplicit() {
		return numExplicit;
	}

	public int ithExplicitIndex(int i) {
		if ((i < 0) || (i >= numExplicit)) {
			throw new IndexOutOfBoundsException("num explicit: " + numExplicit
					+ ", requested index: " + i);
		}
		return i;
	}

	public double ithExplicitValue(int i) {
		return values[i];
	}

	private void ensureIndexReady(int index) {
		if (index >= values.length) {
			// double the allocated space
			double[] newValues = new double[values.length * 2];

			// copy up to numExplicit
			System.arraycopy(values, 0, newValues, 0, numExplicit);

			// change member variable
			values = newValues;
		}
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("[");
		for (int i = 0; i < numExplicit; ++i) {
			buf.append(values[i]);
			if (i + 1 < numExplicit) {
				buf.append(", ");
			}
		}
		buf.append("]");
		return buf.toString();
	}

	private static final int INITIAL_SIZE = 8;

	private double[] values;
	int numExplicit = 0;
}
