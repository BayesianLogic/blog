/*
 * Copyright (c) 2012, Regents of the University of California
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import blog.model.Type;

/**
 * A Dirichlet distribution with shape parameter vector a, defined by
 * f(x1, x2, ... xn) = 1/Z * (x1^(a1-1) * x2^(a2-1) * ... * xn^(an-1),
 * where \sum(xi) = 1 and Z = \prod(Gamma(ai)) / Gamma(\sum(ai)).
 * 
 * Note that this is a generalization of the Beta distribution: while
 * the Beta generates a parameter for the Bernoulli process (Bernoulli
 * and Binomial distributions), the Dirichlet generates parameters for
 * the Categorical process (Categorical and Multinomial distributions).
 */
public class Dirichlet extends AbstractCondProbDistrib {
	
	private Double[] alpha;
	private Gamma[] gammas;
	
	/**
	 * Constructs a Dirichlet distribution with the given parameters.
	 * 
	 * @param a list of parameters for the distribution
	 */
	public Dirichlet(List<Double> params) {
		alpha = new Double[params.size()];
		params.toArray(alpha);
		initGammas();
	}
	
	/**
	 * Constructs a Dirichlet distribution with the given dimension and
	 * parameter value for all dimensions
	 * 
	 * @param the dimension of the distribution
	 * @param the value for all parameters of this distribution
	 */
	public Dirichlet(int dimension, double paramVal) {
		alpha = new Double[dimension];
		Arrays.fill(alpha, paramVal);
		initGammas();
	}
	
	/**
	 * Helper for constructor to construct needed gamma functions
	 */
	private void initGammas() {
		int numParams = alpha.length;
		gammas = new Gamma[numParams];
		for (int i = 0; i < numParams; i++) {
			gammas[i] = new Gamma(alpha[i], 1);
		}
	}

	/**
	 * Returns the probability of a vector of values from this distribution.
	 */
	public double getProb(List args, Object childValue) {
		if (!(childValue instanceof List<?>)) {
			throw new IllegalArgumentException("Dirichlet distribution" +
					"requires List as argument, not " + childValue.getClass() + ".");
		}
		
		List children = (List)childValue;
		if (children.size() == 0 || !(children.get(0) instanceof Number)) {
			throw new IllegalArgumentException("Dirichlet distribution" +
					"requires List of Numbers as argument, not " + children.get(0).getClass() + ".");
		}
		
		List<Number> values = (List<Number>)childValue;
		double prob = 1.0;
		for (int i = 0; i < values.size(); i++) {
			double x = values.get(i).doubleValue();
			prob *= Math.pow(x, alpha[i] - 1);
		}
		prob /= normalize(alpha);
		
		return prob;
	}
	
	/**
	 * Returns the log of the probability of a vector of values from this distribution.
	 */
	public double getLogProb(List args, Object childValue) {
		if (!(childValue instanceof List<?>)) {
			throw new IllegalArgumentException("Dirichlet distribution" +
					"requires List as argument, not " + childValue.getClass() + ".");
		}
		
		List children = (List)childValue;
		if (children.size() == 0 || !(children.get(0) instanceof Number)) {
			throw new IllegalArgumentException("Dirichlet distribution" +
					"requires List of Numbers as argument, not " + children.get(0).getClass() + ".");
		}
		
		List<Number> values = (List<Number>)childValue;
		double prob = 0.0;
		for (int i = 0; i < values.size(); i++) {
			double x = values.get(i).doubleValue();
			prob += Math.log(x) * (alpha[i] - 1);
		}
		prob -= Math.log(normalize(alpha));
		
		return prob;
	}

	/**
	 * Returns a list of doubles sampled from this distribution.
	 */
	public Object sampleVal(List args, Type childType) {
		double sum = 0.0;
		List<Double> samples = new ArrayList<Double>();
		
		List<Object> dummy = new ArrayList<Object>();
		for (Gamma component: gammas) {
			double sample = (Double) component.sampleVal(dummy, childType);
			sum += sample;
			samples.add(sample);
		}
		
		for (int i = 0; i < samples.size(); i++) {
			samples.set(i, samples.get(i) / sum);
		}
		return samples;
	}
	
	/**
	 * Computes the normalization constant for a Dirichlet distribution with
	 * the given parameters.
	 * 
	 * @param a list of parameters of a Dirichlet distribution
	 * @return the normalization constant for such a distribution
	 */
	public static final double normalize(Double[] params) {
		double denom = 0.0;
		double numer = 1.0;
		
		for (double param: params) {
			numer *= Gamma.gamma(param);
			denom += param;
		}
		denom = Gamma.gamma(denom);
		
		return numer / denom;
	}
	
	public String toString() {
		return getClass().getName();
	}
}
