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

import java.io.Serializable;
import java.io.IOException;

/**
 * A distribution over a finite set of elements 0, 1, ..., k, specified with an
 * array of k probabilities pi_0,...,pi_k summing to 1.
 */
public class Multinomial implements Serializable, IntegerDist {
	/**
	 * Creates a Multinomial object representing the uniform distribution over k
	 * elements.
	 */
	public Multinomial(int k) {
		pi = new double[k];
		for (int i = 0; i < k; i++) {
			pi[i] = 1.0 / k;
		}

		counts = new int[k];
	}

	/**
	 * Creates a Multinomial object with probabilities specified by the given
	 * array.
	 * 
	 * @throws IllegalArgumentException
	 *           if pi does not define a probability distribution
	 */
	public Multinomial(double[] pi) {
		this.pi = (double[]) pi.clone();
		double sum = 0;
		for (int i = 0; i < pi.length; i++) {
			if ((pi[i] < 0) || (pi[i] > 1)) {
				throw new IllegalArgumentException("Probability " + pi[i]
						+ " for element " + i + " is not valid.");
			}
			sum += pi[i];
		}
		if (Math.abs(sum - 1) > 1e-9) {
			throw new IllegalArgumentException("Probabilities sum to " + sum
					+ " rather than 1.0.");
		}

		counts = new int[pi.length];
	}

	/**
	 * Returns the size of the set that this distribution is defined over.
	 */
	public int size() {
		return pi.length;
	}

	/**
	 * Returns the probability of element i.
	 */
	public double getProb(int i) {
		return pi[i];
	}

	/**
	 * Returns the log of the probability of element i.
	 */
	public double getLogProb(int i) {
		return Math.log(pi[i]);
	}

	/**
	 * Records an occurrence of element i, for use in updating parameters.
	 */
	public void collectStats(int i) {
		totalCount++;
		counts[i]++;
	}

	/**
	 * Records n occurrences of an element i, for use in updating parameters.
	 */
	public void collectAggrStats(int i, int n) {
		totalCount += n;
		counts[i] += n;
	}

	/**
	 * Sets the parameter array pi to the values that maximize the likelihood of
	 * the elements passed to collectStats since the last call to updateParams.
	 * Then clears the collected statistics, and returns the difference between
	 * the log likelihood of the data under the new parameters and the log
	 * likelihood under the old parameters.
	 */
	public double updateParams() {
		double oldLogProb = 0;
		double newLogProb = 0;

		// Update parameters
		if (totalCount > 0) {
			for (int i = 0; i < counts.length; i++) {
				oldLogProb += (counts[i] * Math.log(pi[i]));
				pi[i] = counts[i] / (double) totalCount;
				newLogProb += (counts[i] * Math.log(pi[i]));
			}
		}

		// Clear statistics
		totalCount = 0;
		for (int i = 0; i < counts.length; i++) {
			counts[i] = 0;
		}

		return (newLogProb - oldLogProb);
	}

	/**
	 * Returns an integer chosen at random according to this distribution.
	 */
	public int sample() {
		double target = Util.random();
		double cumProb = 0;
		for (int i = 0; i < pi.length; i++) {
			cumProb += pi[i];
			if (target < cumProb) {
				return i;
			}
		}
		return (pi.length - 1); // this shouldn't ever be executed
	}

	/**
	 * Called when this object is read in from a stream through the serialization
	 * API. It allocates a <code>counts</code> array of the appropriate size.
	 */
	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		counts = new int[pi.length];
	}

	double[] pi;

	transient int totalCount;
	transient int[] counts;
}
