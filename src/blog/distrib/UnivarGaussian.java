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
 * Gaussian (normal) distribution over real numbers. This CPD can be initialized
 * with zero, one, or two parameters. If two parameters are given, then the
 * first is the mean and the second is the variance. If only one parameter is
 * given, it is interpreted as the variance, and the <code>getProb</code> and
 * <code>sampleVal</code> methods will expect one argument specifying the mean.
 * If no parameters are given, then those methods will expect two arguments, the
 * mean and the variance.
 */
public class UnivarGaussian extends AbstractCondProbDistrib {
	public static final UnivarGaussian STANDARD = new UnivarGaussian(0, 1);

	/**
	 * Creates a univariate Gaussian distribution with the given fixed mean and
	 * variance.
	 */
	public UnivarGaussian(double mean, double variance) {
		fixedMean = true;
		setMean(new Double(mean));

		fixedVariance = true;
		setVariance(new Double(variance));
	}

	/**
	 * Creates a univariate Gaussian distribution. If two parameters are given,
	 * then the first is the mean and the second is the variance. If only one
	 * parameter is given, it is interpreted as the variance. Parameters not
	 * specified here must be given as arguments to <code>getProb</code> and
	 * <code>sampleVal</code>.
	 */
	public UnivarGaussian(List params) {
		if (params.size() == 0) {
			fixedMean = false;
			fixedVariance = false;
		} else if (params.size() == 1) {
			fixedMean = false;
			fixedVariance = true;
			setVariance(params.get(0));
		} else if (params.size() == 2) {
			fixedMean = true;
			setMean(params.get(0));
			fixedVariance = true;
			setVariance(params.get(1));
		} else {
			throw new IllegalArgumentException(
					"UnivarGaussian CPD expects at most 2 parameters, not "
							+ params.size());
		}
	}

	public double getProb(List args, Object value) {
		initParams(args);
		if (!(value instanceof Number))
			throw new IllegalArgumentException(
					"The value passed to the univariate Gaussian distribution's "
							+ "getProb " + "method must be of type Number, not "
							+ value.getClass() + ".");
		return getProb(((Number) value).doubleValue());
	}

	public double getLogProb(List args, Object value) {
		initParams(args);
		if (!(value instanceof Number))
			throw new IllegalArgumentException(
					"The value passed to the univariate Gaussian distribution's "
							+ "getLogProb " + "method must be of type Number, not "
							+ value.getClass() + ".");
		return getLogProb(((Number) value).doubleValue());
	}

	/**
	 * Returns the density of this Gaussian distribution at the given value. This
	 * method should only be called if the mean and variance were set in the
	 * constructor (internal calls are ok if the private method
	 * <code>initParams</code> is called first).
	 */
	public double getProb(double x) {
		return (Math.exp(-Math.pow((x - mu), 2) / (2 * sigmaSquared)) / normConst);
	}

	/**
	 * Returns the natural log of the density of this Gaussian distribution at the
	 * given value. This method should only be called if the meand and variance
	 * were set in the constructor, or if <code>initParams</code> has been called.
	 */
	public double getLogProb(double x) {
		return (-Math.pow((x - mu), 2) / (2 * sigmaSquared)) - logNormConst;
	}

	public Object sampleVal(List args, Type childType) {
		initParams(args);
		return new Double(sampleVal());
	}

	/**
	 * Returns a value sampled from this Gaussian distribution. This method should
	 * only be called if the mean and variance were set in the constructor
	 * (internal calls are ok if the private method <code>initParams</code> is
	 * called first).
	 * 
	 * <p>
	 * The implementation uses the Box-Muller transformation [G.E.P. Box and M.E.
	 * Muller (1958) "A note on the generation of random normal deviates". Ann.
	 * Math. Stat 29:610-611]. See also
	 * http://www.cs.princeton.edu/introcs/26function/MyMath.java.html
	 */
	public double sampleVal() {
		double U = Util.random();
		double V = Util.random();
		return (mu + (sigma * Math.sin(2 * Math.PI * V) * Math.sqrt((-2 * Math
				.log(U)))));
	}

	private void initParams(List args) {
		if (fixedMean) {
			if (args.size() > 0) {
				throw new IllegalArgumentException(
						"UnivarGaussian CPD with fixed mean expects no " + "arguments.");
			}
		} else {
			if (args.size() < 1) {
				throw new IllegalArgumentException(
						"UnivarGaussian CPD created without a fixed mean; "
								+ "requires mean as an argument.");
			}
			setMean(args.get(0));

			if (fixedVariance) {
				if (args.size() > 1) {
					throw new IllegalArgumentException(
							"UnivarGaussian CPD with fixed variance expects "
									+ "only one argument.");
				}
			} else {
				if (args.size() < 2) {
					throw new IllegalArgumentException(
							"UnivarGaussian CPD created without a fixed "
									+ "variance; requires variance as argument.");
				}
				setVariance(args.get(1));
			}
		}
	}

	private void setMean(Object mean) {
		if (!(mean instanceof Number)) {
			throw new IllegalArgumentException(
					"Mean of UnivarGaussian distribution must be a number, " + "not "
							+ mean + " of " + mean.getClass());
		}
		mu = ((Number) mean).doubleValue();
	}

	public double getMean() {
		return mu;
	}

	private void setVariance(Object variance) {
		if (!(variance instanceof Number)) {
			throw new IllegalArgumentException(
					"Variance of UnivarGaussian distribution must be a number, " + "not "
							+ variance + " of " + variance.getClass());
		}
		sigmaSquared = ((Number) variance).doubleValue();

		if (sigmaSquared <= 0) {
			throw new IllegalArgumentException(
					"Variance of UnivarGaussian distribution must be positive, " + "not "
							+ sigmaSquared);
		}
		sigma = Math.sqrt(sigmaSquared);
		normConst = sigma * ROOT_2PI;
		logNormConst = (0.5 * Math.log(sigmaSquared)) + LOG_ROOT_2PI;
	}

	/**
	 * Returns a Gaussian distribution corresponding to the product of this and
	 * another Gaussian distribution.
	 */
	public UnivarGaussian product(UnivarGaussian another) {
		double sumOfSigmaSquares = sigmaSquared + another.sigmaSquared;
		return new UnivarGaussian((mu * another.sigmaSquared + another.mu
				* sigmaSquared)
				/ sumOfSigmaSquares, (sigmaSquared * another.sigmaSquared)
				/ sumOfSigmaSquares);
	}

	/**
	 * Returns the product of a set of UnivarGaussians, returning
	 * <code>null</code> if set is empty.
	 */
	public static UnivarGaussian product(Collection<UnivarGaussian> gaussians) {
		if (gaussians.size() == 0)
			return null;
		Iterator<UnivarGaussian> gaussiansIt = gaussians.iterator();
		UnivarGaussian result = gaussiansIt.next();
		while (gaussiansIt.hasNext()) {
			result = result.product(gaussiansIt.next());
		}
		return result;
	}

	/**
	 * Returns a Gaussian representing the posterior of the mean of this Gaussian
	 * (but ignoring its currently set mean) given a value of its domain.
	 */
	public UnivarGaussian meanPosterior(double value) {
		return new UnivarGaussian(value, sigmaSquared);
	}

	public String toString() {
		return "UnivarGaussian(" + mu + ", " + sigmaSquared + ")";
	}

	private boolean fixedMean;
	private boolean fixedVariance;

	private double mu;
	private double sigmaSquared;
	private double sigma;
	private double normConst;
	private double logNormConst;

	private static final double ROOT_2PI = Math.sqrt(2 * Math.PI);
	private static final double LOG_ROOT_2PI = 0.5 * (Math.log(2) + Math
			.log(Math.PI));
}
