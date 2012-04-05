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
import blog.model.Type;

import java.util.*;

/**
 * A Negative Binomial distribution with parameters k (number of successes) and
 * p (probability of a success at a given trial). If k is an integer, this is
 * also called the Pascal distribution. The distribution is defined (in discrete
 * terms) as the number of failures before k successes.
 */
public class NegativeBinomial extends AbstractCondProbDistrib {
	/**
	 * Creates a new instance of the NegativeBinomial with parameters k and p.
	 */
	public NegativeBinomial(List params) {
		if (!((params.get(0) instanceof Integer) && (params.get(1) instanceof Number))) {
			throw new IllegalArgumentException(
					"NegativeBinomial expects two numerical arguments "
							+ "{k, p} where k is an Integer, p a Double. Got: " + params);
		}
		k = ((Integer) params.get(0)).intValue();
		p = ((Number) params.get(1)).doubleValue();
		gamma = new Gamma(k, (p / (1 - p)));
	}

	/**
	 * Returns the probability of n failures under this distribution.
	 */
	public double getProb(List args, Object value) {
		if (!(value instanceof Integer)) {
			throw new IllegalArgumentException(
					"NegativeBinomial CPD defines a distribution over objects"
							+ " of class Integer, not " + value.getClass() + ".");
		}
		int n = ((Integer) value).intValue();
		// Return C(k+n-1, n)*p^k*(1-p)^n
		return Math.exp(getLogProb(args, value));
	}

	/**
	 * Returns the log of the probability of n failures in this distribution.
	 */
	public double getLogProb(List args, Object value) {
		if (!(value instanceof Integer)) {
			throw new IllegalArgumentException(
					"NegativeBinomial CPD defines a distribution over objects"
							+ " of class Integer, not " + value.getClass() + ".");
		}
		int n = ((Integer) value).intValue();
		// Return log (C(k+n-1, n)*p^k*(1-p)^n)
		return (k * Math.log(p) + n * Math.log(1 - p)
				+ Math.log(Util.factorial(n + k - 1)) - Math.log(Util.factorial(k - 1)) - Math
					.log(Util.factorial(n)));
	}

	/**
	 * Returns a double sampled according to this distribution. Takes time
	 * O(GammaDistrib.sampleVal() + Poisson.sampleVal()). (Reference: A Guide To
	 * Simulation, 2nd Ed. Bratley, Paul, Bennett L. Fox and Linus E. Schrage.)
	 */
	public Object sampleVal(List args, Type childType) {
		Double theta = (Double) gamma.sampleVal(new LinkedList(), childType);
		LinkedList l = new LinkedList();
		l.add(theta);
		Poisson poisson = new Poisson(l);
		return poisson.sampleVal(new LinkedList(), childType);
	}

	public String toString() {
		return getClass().getName();
	}

	private Gamma gamma;
	private int k;
	private double p;
}
