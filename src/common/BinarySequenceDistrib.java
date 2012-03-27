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

import java.io.Serializable;
import java.io.IOException;
import java.util.BitSet;

/**
 * A distribution over sequences of independent, identically distributed binary
 * variables. It has one parameter: the probability that any given element in
 * the sequence is true.
 */
public class BinarySequenceDistrib implements Serializable {
	/**
	 * Creates a BinarySequenceDistrib object with the probability of any element
	 * being <code>true</code> set to 0.5.
	 */
	public BinarySequenceDistrib() {
		probTrue = 0.5;
	}

	/**
	 * Creates a BinarySequenceDistrib object with the probability of any element
	 * being <code>true</code> set to <code>p</code>.
	 * 
	 * @throws IllegalArgumentException
	 *           if p < 0 or p > 1.
	 */
	public BinarySequenceDistrib(double p) {
		probTrue = p;
		if ((p < 0) || (p > 1)) {
			throw new IllegalArgumentException("Illegal probability: " + p);
		}
	}

	/**
	 * Returns the probability of the given array of values.
	 */
	public double getProb(boolean[] x) {
		int numTrue = countTrue(x);
		return (Math.pow(probTrue, numTrue) * Math.pow(1 - probTrue, x.length
				- numTrue));
	}

	/**
	 * Returns the probability of an array of length n, where the set of true
	 * elements is specified by the given BitSet.
	 */
	public double getProb(BitSet s, int n) {
		return (Math.pow(probTrue, s.cardinality()) * Math.pow(1 - probTrue,
				n - s.cardinality()));
	}

	/**
	 * Returns the log of the probability of the given array of values.
	 */
	public double getLogProb(boolean[] x) {
		int numTrue = countTrue(x);
		return ((numTrue * Math.log(probTrue)) + ((x.length - numTrue) * Math
				.log(1 - probTrue)));
	}

	/**
	 * Returns the log probability of an array of length n, where the set of true
	 * elements is specified by the given BitSet.
	 */
	public double getLogProb(BitSet s, int n) {
		return ((s.cardinality() * Math.log(probTrue)) + ((n - s.cardinality()) * Math
				.log(1 - probTrue)));
	}

	/**
	 * Records an occurrence of the array x, for use in updating parameters.
	 */
	public void collectStats(boolean[] x) {
		int numTrue = countTrue(x);
		totalCount += x.length;
		numTrue += numTrue;
	}

	/**
	 * Records the occurrence of an array of length n, where the set of true
	 * elements is specified by the given BitSet.
	 */
	public void collectStats(BitSet s, int n) {
		totalCount += n;
		totalTrue += s.cardinality();
	}

	/**
	 * Sets the parameter probTrue to the value that maximizes the likelihood of
	 * the arrays passed to collectStats since the last call to updateParams. Then
	 * clears the collected statistics, and returns the difference between the log
	 * likelihood of the data under the new parameters and the log likelihood
	 * under the old parameters.
	 */
	public double updateParams() {
		// Update parameters
		double oldLogProb = (totalTrue * Math.log(probTrue))
				+ ((totalCount - totalTrue) * Math.log(1 - probTrue));
		if (totalCount > 0) {
			probTrue = totalTrue / (double) totalCount;
		}
		double newLogProb = (totalTrue * Math.log(probTrue))
				+ ((totalCount - totalTrue) * Math.log(1 - probTrue));

		// Clear statistics
		totalCount = 0;
		totalTrue = 0;

		return (newLogProb - oldLogProb);
	}

	int countTrue(boolean[] x) {
		int numTrue = 0;
		for (int i = 0; i < x.length; i++) {
			if (x[i]) {
				numTrue++;
			}
		}
		return numTrue;
	}

	double probTrue;

	transient int totalCount;
	transient int totalTrue;
}
