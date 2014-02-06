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
 * A mixture of a multinomial over the first k natural numbers 0,...,(k-1), and
 * a geometric distribution over the numbers >= k. The parameter k is fixed at
 * construction. The one free parameter is lambda = P(X < k).
 */
public class MultinomialWithTail implements Serializable, IntegerDist {
	/**
	 * Creates a MultinomialWithTail with the given k. The multinomial
	 * distribution is initially uniform, the geometric distribution has alpha =
	 * 0.5, and lambda is also initialized to 0.5.
	 */
	public MultinomialWithTail(int k) {
		this.k = k;
		multinomial = new Multinomial(k);
		geometric = new Geometric();
		lambda = 0.5;
		cacheParams();
	}

	/**
	 * Creates a MultinomialWithTail with the given array of probabilities for the
	 * multinomial distribution, and the given lambda and alpha values. k is set
	 * to the size of the probability array.
	 * 
	 * @param pi
	 *          an array of double values specifying a probability distribution
	 *          over the first pi.length natural numbers
	 * @param lambda
	 *          the probability P(X < pi.length)
	 * @param alpha
	 *          the parameter of the geometric distribution: P(X >= n+1 | X >= n)
	 *          for n >= pi.length
	 */
	public MultinomialWithTail(double[] pi, double lambda, double alpha) {
		k = pi.length;
		multinomial = new Multinomial(pi);
		geometric = new Geometric(alpha);

		if ((lambda < 0) || (lambda > 1)) {
			throw new IllegalArgumentException("Mixing parameter lambda must "
					+ "be between 0 and 1");
		}
		this.lambda = lambda;
		cacheParams();
	}

	/**
	 * Returns the log probability of the number n.
	 */
	public double getLogProb(int n) {
		if (n < 0) {
			return 0;
		}
		if (n < k) {
			return (logLambda + multinomial.getLogProb(n));
		}
		return (logOneMinusLambda + geometric.getLogProb(n - k));
	}

	/**
	 * Return probability of n
	 */
	public double getProb(int n) {
		return Math.exp(getLogProb(n));
	}

	/**
	 * Records an occurrence of the number n, for use in updating parameters.
	 */
	public void collectStats(int n) {
		if (n < 0) {
			throw new IllegalArgumentException(
					"MultinomialWithTail can't generate a negative number.");
		}

		totalCount++;
		if (n < k) {
			multinomialCount++;
			multinomial.collectStats(n);
		} else {
			geometric.collectStats(n - k);
		}
	}

	/**
	 * Generates iid samples from this distribution
	 */
	public int sample() {
		if (Util.random() < lambda) {
			// X < k
			return multinomial.sample();
		} else {
			// X >= k
			return geometric.sample() + k;
		}
	}

	/**
	 * Sets the parameter lambda to the value that maximizes the likelihood of the
	 * numbers passed to collectStats since the last call to updateParams. Then
	 * clears the collected statistics, and returns the difference between the log
	 * likelihood of the data under the new parameters and the log likelihood
	 * under the old parameters.
	 */
	public double updateParams() {
		// Update mixture parameter
		double oldLogProb = (multinomialCount * logLambda)
				+ ((totalCount - multinomialCount) * logOneMinusLambda);
		if (totalCount > 0) {
			lambda = multinomialCount / (double) totalCount;
			cacheParams();
		}
		double newLogProb = (multinomialCount * logLambda)
				+ ((totalCount - multinomialCount) * logOneMinusLambda);

		// Update parameters for component distributions
		double logProbChange = newLogProb - oldLogProb;
		logProbChange += multinomial.updateParams();
		logProbChange += geometric.updateParams();

		// Clear statistics
		totalCount = 0;
		multinomialCount = 0;

		return logProbChange;
	}

	/**
	 * Called when this object is read in from a stream through the serialization
	 * API.
	 */
	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		cacheParams();
	}

	void cacheParams() {
		logLambda = Math.log(lambda);
		logOneMinusLambda = Math.log(1 - lambda);
	}

	int k;
	Multinomial multinomial;
	Geometric geometric;

	double lambda;
	transient double logLambda;
	transient double logOneMinusLambda;

	transient int totalCount;
	transient int multinomialCount;
}
