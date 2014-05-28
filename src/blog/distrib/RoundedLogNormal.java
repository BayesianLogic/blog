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

package blog.distrib;

import java.util.*;
import blog.*;

/**
 * Distribution over positive integers for a random variable X = round(Y), where
 * Y has a log normal distribution. This means Z = log(Y) has a normal
 * distribution. A RoundedLogNormal has two parameters, the mean and the
 * variance. For consistency with Hanna's code, we use the mean of Y and the
 * variance of Z.
 */
public class RoundedLogNormal extends AbstractCondProbDistrib {
	/**
	 * Creates a RoundedLogNormal distribution where Y has the given mean and
	 * log(Y) has the given variance.
	 */
	public RoundedLogNormal(double mean, double varianceOfLog) {
		this.mean = mean;
		this.varianceOfLog = varianceOfLog;
		zDistrib = new UnivarGaussian(Math.log(mean), varianceOfLog);
	}

	/**
	 * Creates a RoundedLogNormal distribution with the given parameters. This
	 * method expects two parameters of class Number, namely the mean of Y and the
	 * variance of log(Y).
	 */
	public RoundedLogNormal(List params) {
		if (params.size() != 2) {
			throw new IllegalArgumentException(
					"RoundedLogNormal expects two parameters: the mean of Y "
							+ "and the variance of log(Y).");
		}

		mean = ((Number) params.get(0)).doubleValue();
		varianceOfLog = ((Number) params.get(1)).doubleValue();
		zDistrib = new UnivarGaussian(Math.log(mean), varianceOfLog);
	}

	/**
	 * Returns the probability of the given value under this distribution. Expects
	 * no arguments.
	 */
	public double getProb(List args, Object value) {
		return Math.exp(getLogProb(args, value));
	}

	/**
	 * Returns the log probability of the given value under this distribution.
	 * Expects no arguments.
	 */
	public double getLogProb(List args, Object value) {
		if (!args.isEmpty()) {
			throw new IllegalArgumentException(
					"RoundedLogNormal expects no arguments.");
		}
		return getLogProb(((Integer) value).intValue());
	}

	/**
	 * Returns the log probability that X=n. Note that X gets the value n if Y is
	 * between n - 0.5 and n + 0.5, which means Z is between log(n - 0.5) and
	 * log(n + 0.5). So we should integrate the density of Z between those two
	 * values. To avoid computing the integral, we approximate this by taking the
	 * density of Z at log(n) and multiplying it by log(n + 0.5) - log(n - 0.5).
	 */
	public double getLogProb(int n) {
		if (n <= 0) {
			return Double.NEGATIVE_INFINITY;
		}

		double intervalWidth = Math.log(n + 0.5) - Math.log(n - 0.5);
		return Math.log(intervalWidth) + zDistrib.getLogProb(Math.log(n));
	}

	public Object sampleVal(List args) {
		if (!args.isEmpty()) {
			throw new IllegalArgumentException(
					"RoundedLogNormal expects no arguments.");
		}

		double z = zDistrib.sampleVal();
		double y = Math.exp(z);
		long x = Math.round(y);
		if ((x < Integer.MIN_VALUE) || (x > Integer.MAX_VALUE)) {
			throw new RuntimeException(
					"Value sampled from RoundedLogNormal distribution is "
							+ "too great in magnitude to be represented as an Integer.");
		}
		return new Integer((int) x);
	}

	double mean;
	double varianceOfLog;

	UnivarGaussian zDistrib;
}
