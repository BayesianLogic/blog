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
import java.util.*;
import Jama.*;

/**
 * Gaussian (normal) distribution over real vectors of some fixed 
 * dimensionality <i>d</i>.  This CPD can be initialized with one, two, or 
 * three parameters.  If three parameters are given, then they are the 
 * dimension, mean, and covariance matrix.  If two parameters are given, 
 * they are interpreted as the dimension and covariance matrix; in this 
 * case the mean must be passed as an argument to the <code>getProb</code> 
 * and <code>sampleVal</code> methods.  If only one parameter is given, it 
 * is the dimension, and the mean and covariance matrix must both be given 
 * as arguments.
 */
public class MultivarGaussian extends AbstractCondProbDistrib {

    /**
     * Creates a new MultivarGaussian distribution with the given mean 
     * vector and covariance matrix.  The dimension is inferred from 
     * the length of the mean vector.  
     *
     * @param mean       1-by-d mean vector
     * @param covariance d-by-d covariance matrix 
     */
    public MultivarGaussian(Matrix mean, Matrix covariance) {
	setDimension(mean.getRowDimension());
	fixedMean = true;
	setMean(mean);
	fixedCovariance = true;
	setCovariance(covariance);
    }
   
    /** Sets mean and covariance and ensures that their dimensions match. */
    public MultivarGaussian(List params){
	if (params.size() == 0) {
	    throw new IllegalArgumentException
		("Dimension of MultivarGaussian distribution must be "
		 + "specified as parameter.");
	} 
	if (!(params.get(0) instanceof Integer)) {
		throw new IllegalArgumentException
		    ("Dimension of MultivarGaussian distribution must be an "
		     + "integer, not " + params.get(0) + " of " 
		     + params.get(0).getClass());
	}
	setDimension(((Integer) params.get(0)).intValue());

	if (params.size() == 1) {
	    fixedMean = false;
	    fixedCovariance = false;
	} else if (params.size() == 2) {
	    fixedMean = false;
	    fixedCovariance = true;
	    setCovariance(params.get(1));
	} else if (params.size() == 3) {
	    fixedMean = true;
	    setMean(params.get(1));
	    fixedCovariance = true;
	    setCovariance(params.get(2));
	} else {
	    throw new IllegalArgumentException
		("MultivarGaussian CPD expects at most 3 parameters, not " 
		 + params.size());
	}
    }


    /** 
     * Ensures that x = value is a column matrix of appropriate dimension d and
     * returns the density of this Gaussian distribution at x.
     */ 
    public double getProb(List args, Object value){
	initParams(args);

	if (!((value instanceof Matrix) 
	     && (((Matrix)value).getRowDimension() == d) 
	     && (((Matrix)value).getColumnDimension() == 1)))
	    throw new IllegalArgumentException
		("The value passed to the " + d + "-dimensional "
		 + "multivariate Gaussian distribution's getProb "
		 + "method must be a column vector of length " + d
		 + ", not " + value);

	return getProbInternal((Matrix) value);
    }

    /**
     * Given a d-dimensional column vector x, returns the density value 
     * p = 1/sqrt((2*pi)^d*|sigma|)*exp{-0.5(x-mean)'*inverse(sigma)*(x-mean)}
     *
     * @throws IllegalStateException if this distribution does not have fixed 
     *                               mean and covariance
     */
    public double getProb(Matrix x) {
	if (!fixedMean || !fixedCovariance) {
	    throw new IllegalStateException
		("Mean and covariance are not fixed.");
	}
	return getProbInternal(x);
    }

    /**
     * Returns the natural log of the probability returned by getProb.
     */
    public double getLogProb(Matrix x) {
	if (!fixedMean || !fixedCovariance) {
	    throw new IllegalStateException
		("Mean and covariance are not fixed.");
	}
	return getLogProbInternal(x);
    }

    /**
     * Samples a value from this multivariate Gaussian by generating <i>d
     * </i> independent samples from univariate Gaussians with unit variance, 
     * one for each dimension, and multiplying the obtained 
     * vector on the left by the square root of sigma (Cholesky decomposition
     * of sigma).  This method should only be called if this distribution 
     * was constructed with a fixed mean and covariance matrix (internal 
     * calls are ok if the private method <code>initParams</code> is called 
     * first). 
     */
    public Object sampleVal(List args, Type childType){
	initParams(args);
	return sampleVal();
    }

    /**
     * Samples a value from this multivariate Gaussian by generating <i>d
     * </i> independent samples from univariate Gaussians with unit variance, 
     * one for each mean in the mean vector, and multiplying the obtained 
     * vector on the left by the square root of sigma (Cholesky decomposition
     * of sigma).  This method should only be called if this distribution 
     * was constructed with a fixed mean and covariance matrix (internal 
     * calls are ok if the private method <code>initParams</code> is called 
     * first). 
     */
    public Matrix sampleVal() {
	Matrix temp = new Matrix(d, 1);
	for (int i = 0; i < d; i++){
	    temp.set(i,0, UnivarGaussian.STANDARD.sampleVal());
	}
	return mu.plus(sqrtSigma.times(temp));
    }

    /**
     * Returns the mean of this distribution, or null if the mean is not 
     * fixed.
     */
    public Matrix getMean() {
	if (fixedMean) {
	    return mu;
	}
	return null;
    }

    /**
     * Returns the covariance matrix of this distribution, or null if the 
     * covariance is not fixed.
     */
    public Matrix getCovar() {
	if (fixedCovariance) {
	    return sigma;
	}
	return null;
    }

    /**
     * Given a d-dimensional column vector x, returns the density value 
     * p = 1/sqrt((2*pi)^d*|sigma|)*exp{-0.5(x-mean)'*inverse(sigma)*(x-mean)}
     */
    private double getProbInternal(Matrix x) {
	return Math.exp(-0.5*x.minus(mu).transpose().times
		  (sigmaInverse).times(x.minus(mu)).get(0,0)) / normConst;
    }

    private double getLogProbInternal(Matrix x) {
	return ((-0.5 * x.minus(mu).transpose().times(sigmaInverse)
		 .times(x.minus(mu)).get(0,0)) 
		- Math.log(normConst));
    }

    private void initParams(List args) {
	if (fixedMean) {
	    if (args.size() > 0) {
		throw new IllegalArgumentException
		    ("MultivarGaussian CPD with fixed mean expects no "
		     + "arguments.");
	    }
	} else {
	    if (args.size() < 1) {
		throw new IllegalArgumentException
		    ("MultivarGaussian CPD created without a fixed mean; "
		     + "requires mean as an argument.");
	    }
	    setMean(args.get(0));

	    if (fixedCovariance) {
		if (args.size() > 1) {
		    throw new IllegalArgumentException
			("MultivarGaussian CPD with fixed covariance matrix "
			 + "expects only one argument.");
		}
	    } else {
		if (args.size() < 2) {
		    throw new IllegalArgumentException
			("MultivarGaussian CPD created without a fixed "
			 + "covariance matrix; requires covariance matrix "
			 + "as argument.");
		}
		setCovariance(args.get(1));
	    }
	}
    }

    private void setDimension(int dim) {
	if (dim <= 0) {
	    throw new IllegalArgumentException
		("Dimension of MultivarGaussian distribution must be "
		 + "positive, not " + dim);
	}

	d = dim;
	dimFactor = Math.pow(2 * Math.PI, d / 2.0);
    }

    private void setMean(Object mean) {
	if (!((mean instanceof Matrix) 
	      && (((Matrix) mean).getColumnDimension() == 1))){
	    throw new IllegalArgumentException 
		("The mean of a MultivarGaussian distribution must be a "
		 + "column vector, not " + mean + " of " + mean.getClass());
	}
	
	mu = (Matrix) mean;

	if (mu.getRowDimension() != d) {
	    throw new IllegalArgumentException
		("Mean of " + d + "-dimensional Gaussian distribution must "
		 + "be column vector of length " + d);
	}
    }

    private void setCovariance(Object cov) {
	if (!((cov instanceof Matrix)
	      && (((Matrix) cov).getColumnDimension() == d)
	      && (((Matrix) cov).getColumnDimension() == d))) {
	    throw new IllegalArgumentException 
		("The covariance matrix of a " + d + "-dimensional Gaussian "
		 + "distribution must be a " + d + "-by-" + d + " Matrix, "
		 + "not " + cov + " of " + cov.getClass());
	}

	sigma = (Matrix) cov;

	for (int i = 0; i < sigma.getRowDimension(); i++){
	    for (int j = 0; j < sigma.getColumnDimension(); j++){
		double ratio = sigma.get(i, j) / sigma.get(j, i);
		if (Math.abs(ratio - 1) > 1e-6)
		    throw new IllegalArgumentException
			("Invalid covariance matrix (not symmetric): " 
			 + sigma);
	    }
	}

	normConst = Math.sqrt(sigma.det()) * dimFactor;
	sigmaInverse = sigma.inverse();
	sqrtSigma = sigma.chol().getL();
    }

    private boolean fixedMean;
    private boolean fixedCovariance;

    private int d;
    private Matrix mu;
    private Matrix sigma;

    private double dimFactor;
    private double normConst;
    private Matrix sigmaInverse;
    private Matrix sqrtSigma;
}
