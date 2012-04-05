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
 * A geometric distribution over the natural numbers 0, 1, 2,... It has a single
 * parameter alpha, which equals P(X >= n+1 | X >= n). Thus an alpha close to 1
 * yields a relatively flat distribution, whereas an alpha close to 0 yields a
 * distribution that decays quickly. The distribution is defined by: P(X = n) =
 * (1 - alpha) alpha^n
 */
public class Geometric implements Serializable, IntegerDist {
	/**
	 * Creates a geometric distribution with alpha = 0.5.
	 */
	public Geometric() {
		this(0.5);
	}

	/**
	 * Creates a geometric distribution with the given alpha parameter. Throws an
	 * IllegalArgumentException if alpha < 0 or alpha >= 1.
	 */
	public Geometric(double alpha) {
		if ((alpha < 0) || (alpha >= 1)) {
			throw new IllegalArgumentException(
					"Illegal alpha parameter for geometric distribution.");
		}

		this.alpha = alpha;
		cacheParams();
	}

	/**
	 * Returns the probability of the number n.
	 */
	public double getProb(int n) {
		if (n < 0) {
			return 0;
		}
		return ((1 - alpha) * Math.pow(alpha, n));
	}

	/**
	 * Returns the log probability of the number n.
	 */
	public double getLogProb(int n) {
		if (n < 0) {
			return Double.NEGATIVE_INFINITY;
		}
		return (logOneMinusAlpha + (n * logAlpha));
	}

	/**
	 * Returns the probability that X = n given that X <= upper. The conditional
	 * distribution of X given X <= upper is called a truncated geometric
	 * distribution. P(X = n | X <= m) = (1 - alpha) * alpha^n / (1 - alpha^(m+1))
	 */
	public double getProbGivenUpperBound(int n, int upper) {
		if (upper < 0) {
			throw new IllegalArgumentException("Conditioning on zero-"
					+ "probability event: X = " + upper);
		}
		if ((n < 0) || (n > upper)) {
			return 0;
		}
		return ((1 - alpha) * Math.pow(alpha, n) / (1 - Math.pow(alpha, upper + 1)));
	}

	/**
	 * Returns the log probability that X = n given that X <= upper. The
	 * conditional distribution of X given X <= upper is called a truncated
	 * geometric distribution. P(X = n | X <= m) = (1 - alpha) * alpha^n / (1 -
	 * alpha^(m+1))
	 */
	public double getLogProbGivenUpperBound(int n, int upper) {
		if (upper < 0) {
			throw new IllegalArgumentException("Conditioning on zero-"
					+ "probability event: X = " + upper);
		}
		if ((n < 0) || (n > upper)) {
			return Double.NEGATIVE_INFINITY;
		}
		return (logOneMinusAlpha + (n * logAlpha) - Math.log(1 - Math.pow(alpha,
				upper + 1)));
	}

	/**
	 * Records an occurrence of the number n, for use in updating parameters.
	 */
	public void collectStats(int n) {
		if (n < 0) {
			throw new IllegalArgumentException(
					"Geometric distribution can't generate a negative number.");
		}

		count++;
		sum += n;
	}

	/**
	 * Sets the parameter alpha to the value that maximizes the likelihood of the
	 * numbers passed to collectStats since the last call to updateParams. Then
	 * clears the collected statistics, and returns the difference between the log
	 * likelihood of the data under the new parameters and the log likelihood
	 * under the old parameters.
	 */
	public double updateParams() {
		// Update parameter
		double oldLogProb = (count * logOneMinusAlpha) + (sum * logAlpha);
		if (count > 0) {
			double mean = sum / (double) count;
			alpha = mean / (1 + mean);
			cacheParams();
		}
		double newLogProb = (count * logOneMinusAlpha) + (sum * logAlpha);

		// Clear statistics
		count = 0;
		sum = 0;

		return (newLogProb - oldLogProb);
	}

	/**
	 * Generate iid samples from this distribution
	 */
	public int sample() {
		double x = Util.random();
		int k = 0;
		double y = 1 - alpha;
		double p = alpha;

		while (y < x) {
			y += (1 - alpha) * p;
			p *= alpha;
			k++;
		}

		return k;

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
		logAlpha = Math.log(alpha);
		logOneMinusAlpha = Math.log(1 - alpha);
	}

	double alpha;
	transient double logAlpha;
	transient double logOneMinusAlpha;

	transient int count;
	transient int sum;
}
