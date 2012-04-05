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

import blog.*;
import blog.common.Util;

import java.util.*;

/**
 * A Binomial distribution with parameters n (number of trials) and p
 * (probability of success for a given trial). The probability of k successes
 * P(k)= C(n,k) * p^k * (1-p)^(n-k). A Binomial distribution can be created in
 * the following ways:
 * <ul>
 * <li>With two parameters, n and p, in which case it expects no arguments;
 * <li>With one parameter, p, in which case it expects n as an argument;
 * <li>With no parameters, in which case it expects two arguments, n and p.
 * </ul>
 */

public class Binomial extends AbstractCondProbDistrib {
	/**
	 * Creates a new Binomial distribution with parameters n and p.
	 */
	public Binomial(int n, double p) {
		nFixed = true;
		this.n = n;
		pFixed = true;
		this.p = p;
	}

	/**
	 * Creates a new Binomial distribution. If two parameters are given, they are
	 * interpreted as n and p. If one parameter is given, it is interpreted as p
	 * and this distribution expects n as an argument. If no parameters are given,
	 * this distribution expects both n and p as arguments.
	 */
	public Binomial(List params) {
		if (params.size() == 0) {
			nFixed = false;
			pFixed = false;
		} else if (params.size() == 1) {
			nFixed = false;
			pFixed = true;
			setP(params.get(0));
		} else if (params.size() == 2) {
			nFixed = true;
			setN(params.get(0));
			pFixed = true;
			setP(params.get(1));
		} else {
			throw new IllegalArgumentException(
					"Binomial CPD expects at most two parameters, n and p.");
		}
	}

	/**
	 * Returns the probability of integer k under this distribution.
	 */
	public double getProb(List args, Object value) {
		processArgs(args);
		if (!(value instanceof Integer)) {
			throw new IllegalArgumentException(
					"Binomial CPD defines a distribution over objects"
							+ " of class Integer, not " + value.getClass() + ".");
		}
		int k = ((Integer) value).intValue();
		return ((Util.factorial(n) / (Util.factorial(k) * Util.factorial(n - k)))
				* Math.pow(p, k) * Math.pow((1 - p), (n - k)));
	}

	/**
	 * Returns the log of the probability of integer k under the distribution.
	 */
	public double getLogProb(List args, Object value) {
		processArgs(args);
		if (!(value instanceof Integer)) {
			throw new IllegalArgumentException(
					"Binomial CPD defines a distribution over objects"
							+ " of class Integer, not " + value.getClass() + ".");
		}
		return Math.log(getProb(args, value));
	}

	/**
	 * Returns an integer sampled according to this distribution. Takes time
	 * proprotional to np + 1. (Reference: Non-Uniform Random Variate Generation,
	 * Devroye http://cgm.cs.mcgill.ca/~luc/rnbookindex.html) Second time-waiting
	 * algorithm.
	 */
	public Object sampleVal(List args, Type childType) {
		processArgs(args);

		double q = -Math.log(1 - p);
		double sum = 0;
		int x = 0;
		double u, e;
		while (sum <= q) {
			u = Util.random();
			e = -Math.log(u); // exponential random variate
			sum += (e / (n - x));
			x += 1;
		}
		return new Integer(x - 1);
	}

	public String toString() {
		return getClass().getName();
	}

	private void processArgs(List args) {
		if (!pFixed) {
			if (args.size() != 2) {
				throw new IllegalArgumentException(
						"Binomial distribution created with no parameters "
								+ "expects n and p as arguments.");
			}
			setN(args.get(0));
			setP(args.get(1));
		} else if (!nFixed) {
			if (args.size() != 1) {
				throw new IllegalArgumentException(
						"Binomial distribution created with no \"n\" parameter "
								+ "expects n as an argument.");
			}
			setN(args.get(0));
		} else if (args.size() != 0) {
			throw new IllegalArgumentException(
					"Binomial distribution created with two parameters "
							+ "expects no arguments.");
		}
	}

	private void setN(Object obj) {
		if (!(obj instanceof Integer)) {
			throw new IllegalArgumentException(
					"Number of trials (n) in binomial distribution must "
							+ "be an integer, not " + obj.getClass());
		}
		n = ((Integer) obj).intValue();
		if (n < 0) {
			throw new IllegalArgumentException(
					"Number of trials (n) in binomial distribution cannot "
							+ "be negative.");
		}
	}

	private void setP(Object obj) {
		if (!(obj instanceof Number)) {
			throw new IllegalArgumentException(
					"Success probability (p) in binomial distribution must "
							+ "be a number, not " + obj.getClass());
		}
		p = ((Number) obj).doubleValue();
		if ((p < 0) || (p > 1)) {
			throw new IllegalArgumentException(
					"Illegal success probability for binomial disribution: " + p);
		}
	}

	private boolean nFixed;
	private int n;
	private boolean pFixed;
	private double p;
}
