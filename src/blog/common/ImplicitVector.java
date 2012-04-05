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

/**
 * A sequence of floating-point numbers that is in principle infinite, but in
 * which only a finite set of entries are represented explicitly. All entries
 * that are not represented explicitly are assumed to be zero (some of the
 * explicit entries may be zero as well).
 */
public abstract class ImplicitVector {
	/**
	 * Returns the value at the given index. This is zero if no value is
	 * explicitly stored for the given index.
	 */
	public abstract double get(int i);

	/*
	 * Sets the value at the given index. If the specified value is non-zero, then
	 * the result is stored explicitly. If the value is zero, it may or may not be
	 * stored explicitly, depending on the implementation.
	 */
	public abstract void set(int i, double value);

	/**
	 * Returns the number of explicitly represented entries in this vector.
	 */
	public abstract int numExplicit();

	/**
	 * Returns the largest index at which a value is explicitly stored.
	 */
	public int maxExplicitIndex() {
		return ithExplicitIndex(numExplicit() - 1);
	}

	/**
	 * Returns the ith explicitly represented index in this vector.
	 * 
	 * @throws IndexOutOfBoundsException
	 *           if <code>i</code> is less than zero or greater than or equal to
	 *           the number of explicitly represented entries in this vector
	 */
	public abstract int ithExplicitIndex(int i);

	/**
	 * Returns the value at the ith explicitly represented index.
	 * 
	 * @throws IndexOutOfBoundsException
	 *           if <code>i</code> is less than zero or greater than or equal to
	 *           the number of explicitly represented entries in this vector
	 */
	public abstract double ithExplicitValue(int i);

	/**
	 * Returns the sum of the entries in this vector.
	 */
	public double sum() {
		double sum = 0;
		for (int i = 0; i < numExplicit(); ++i) {
			sum += ithExplicitValue(i);
		}
		return sum;
	}

	/**
	 * Returns the dot product of this vector with the given vector. The dot
	 * product is the sum, over those indices that are explicitly represented in
	 * both vectors, of the product of the values in the two vectors at that
	 * index.
	 */
	public double dotProduct(ImplicitVector other) {
		ImplicitVector smaller;
		ImplicitVector larger;
		if (numExplicit() <= other.numExplicit()) {
			smaller = this;
			larger = other;
		} else {
			smaller = other;
			larger = this;
		}

		double sum = 0;
		for (int i = 0; i < smaller.numExplicit(); ++i) {
			int index = smaller.ithExplicitIndex(i);
			double valueInLarger = larger.get(index);
			if (valueInLarger != 0) {
				sum += (valueInLarger * smaller.ithExplicitValue(i));
			}
		}
		return sum;
	}

	/**
	 * Returns the dot product of this vector with the given array of double
	 * values. This is the same as calling dotProduct with an ImplicitVector whose
	 * explicit entries are exactly those in the given array.
	 */
	public double dotProduct(double[] arr) {
		double sum = 0;
		for (int i = 0; i < numExplicit(); ++i) {
			int index = ithExplicitIndex(i);
			if (index >= arr.length) {
				break;
			}
			sum += (arr[index] * ithExplicitValue(i));
		}
		return sum;
	}

	/**
	 * Returns an index sampled according to the probability distribution
	 * represented this by this vector. Assumes the entries in this vector are
	 * non-negative and sum to 1.
	 * 
	 * @throws IllegalStateException
	 *           if the sum of the probabilities is less than 1 - Util.TOLERANCE.
	 */
	public int sampleIndex() {
		double u = Util.random();
		double sum = 0;
		for (int i = 0; i < numExplicit(); ++i) {
			sum += ithExplicitValue(i);
			if (sum >= u) {
				return ithExplicitIndex(i);
			}
		}

		if (sum < 1 - Util.TOLERANCE) {
			throw new IllegalStateException("Vector sum is too small: " + sum);
		}
		return ithExplicitIndex(numExplicit() - 1);
	}
}
