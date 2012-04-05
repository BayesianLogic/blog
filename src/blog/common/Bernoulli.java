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
 * A distribution over {true, false}. It has one parameter, the probability of
 * the value <code>true</code>.
 */
public class Bernoulli implements Serializable {
	/**
	 * Creates a Bernoulli object with the probability of <code>true</code> set to
	 * 0.5.
	 */
	public Bernoulli() {
		probTrue = 0.5;
	}

	/**
	 * Creates a Bernoulli object with the probability of <code>true</code> set to
	 * <code>p</code>.
	 * 
	 * @throws IllegalArgumentException
	 *           if p < 0 or p > 1.
	 */
	public Bernoulli(double p) {
		probTrue = p;
		if ((p < 0) || (p > 1)) {
			throw new IllegalArgumentException("Illegal probability: " + p);
		}
	}

	/**
	 * Returns the probability of the Boolean value x.
	 */
	public double getProb(boolean x) {
		return (x ? probTrue : (1 - probTrue));
	}

	/**
	 * Returns the log of the probability of the Boolean value x.
	 */
	public double getLogProb(boolean x) {
		return Math.log(getProb(x));
	}

	/**
	 * Records an occurrence of the value x, for use in updating parameters.
	 */
	public void collectStats(boolean x) {
		totalCount++;
		if (x) {
			numTrue++;
		}
	}

	/**
	 * Sets the parameter probTrue to the value that maximizes the likelihood of
	 * the values passed to collectStats since the last call to updateParams. Then
	 * clears the collected statistics, and returns the difference between the log
	 * likelihood of the data under the new parameters and the log likelihood
	 * under the old parameters.
	 */
	public double updateParams() {
		// Update parameters
		double oldLogProb = (numTrue * Math.log(probTrue))
				+ ((totalCount - numTrue) * Math.log(1 - probTrue));
		if (totalCount > 0) {
			probTrue = numTrue / (double) totalCount;
		}
		double newLogProb = (numTrue * Math.log(probTrue))
				+ ((totalCount - numTrue) * Math.log(1 - probTrue));

		// Clear statistics
		totalCount = 0;
		numTrue = 0;

		return (newLogProb - oldLogProb);
	}

	double probTrue;

	transient int totalCount;
	transient int numTrue;
}
