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

import blog.common.numerical.MatrixFactory;
import blog.common.numerical.MatrixLib;

/**
 * Multivariate Gaussian (normal) distribution over real vectors of some fixed
 * dimensionality <i>d</i> with parameters <code>mean</code> and
 * <code>covariance</code>.
 * 
 * @since June 12, 2014
 */
public class MultivarGaussian implements CondProbDistrib {

  /** Returns the parameter <code>mean</code>. */
  public MatrixLib getMean() {
    return mean;
  }

  /** Returns the parameter <code>covariance</code>. */
  public MatrixLib getCovariance() {
    return covariance;
  }

  /**
   * Mapping for parameter <code>params</code>
   * <ul>
   * <li>params[0]: <code>mean</code>, as a MatrixLib that serves as a column
   * vector</li>
   * <li>params[1]: <code>covariance</code>, as a MatrixLib</li>
   * </ul>
   * 
   * @see blog.distrib.CondProbDistrib#setParams(java.util.List)
   */
  @Override
  public void setParams(Object[] params) {
    if (params.length != 2) {
      throw new IllegalArgumentException("expected two parameters");
    }
    setParams((MatrixLib) params[0], (MatrixLib) params[1]);
  }

  /**
   * If the method parameter mean is non-null and is a legal column vector, set
   * the distribution parameter <code>mean</code> to the method parameter mean.
   * If the method parameter covariance is non-null and is a valid square,
   * symmetric matrix, set the distribution parameter <code>covariance</code> to
   * the method parameter <code>covariance</code>. If both parameters are set,
   * checks to see if dimensions of the mean and covariance agree.
   * 
   * @param mean
   *          parameter <code>mean</code>
   * @param covariance
   *          parameter <code>covariance</code>
   */
  public void setParams(MatrixLib mean, MatrixLib covariance) {
    if (mean != null) {
      if (!(mean.numCols() == 1 && mean.numRows() > 0)) {
        throw new IllegalArgumentException(
            "The mean given is not a valid column vector. It has dimensions "
                + mean.numRows() + " by " + mean.numCols() + ".");
      }
      this.mean = mean;
      this.hasMean = true;
      this.legalAssignment = false;
    }
    if (covariance != null) {
      if (!(covariance.numCols() > 0 && covariance.isSymmetric())) {
        throw new IllegalArgumentException(
            "The covariance matrix given is not symmetric");
      }
      this.covariance = covariance;
      this.hasCovariance = true;
      this.legalAssignment = false;
    }
    if (!this.legalAssignment && !this.hasMean && !this.hasCovariance) {
      if (this.covariance.numCols() != this.mean.numCols()) {
        throw new IllegalArgumentException(
            "Dimensions of the mean vector and the covariance matrix do not match: "
                + "The covariance matrix is " + covariance.numCols() + " by "
                + covariance.numRows() + " while the mean vector is "
                + mean.numRows() + " by " + mean.numCols() + ".");
      }
      initializeConstants();
    }
  }

  /**
   * Precondition: The current covariance and mean parameters constitute a legal
   * assignment.
   * 
   * Initializes the following constants for the Multivariate Gaussian:
   * <ul>
   * <li>dimension</li>
   * <li>dimension factor</li>
   * <li>log of the dimensions factor</li>
   * <li>normalization constant</li>
   * <li>log of the normalization constant</li>
   * <li>inverse of the covariance matrix</li>
   * <li>square root of the covariance matrix</li>
   * </ul>
   * dimension
   * factor, log of the dimension factor, normalization constant, log of the
   * normalization constant, inverse of
   */
  private void initializeConstants() {
    this.d = mean.numCols();
    this.dimFactor = Math.pow(2 * Math.PI, d / 2.0);
    this.logDimFactor = Math.log(2 * Math.PI) * d / 2.0;

    this.normConst = Math.sqrt(covariance.det()) * dimFactor;
    this.logNormConst = 0.5 * covariance.logDet() + logDimFactor;
    this.inverseCovariance = covariance.inverse();
    this.sqrtCovariance = covariance.choleskyFactor();
  }

  private void checkHasParams() {
    if (!this.legalAssignment) {
      throw new IllegalArgumentException(
          "The current mean and covariance are not legal");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#getProb(java.lang.Object)
   */
  @Override
  public double getProb(Object value) {
    return getProb((MatrixLib) value);
  }

  /**
   * If <code>x</code> is of the correct dimension and the mean and covariance
   * have been initialized correctly, return the density value p =
   * 1/sqrt((2*pi)^d*|sigma|)*exp{-0.5(x-mean)'*inverse(sigma)*(x-mean)}
   * 
   * @throws IllegalStateException
   *           if the matrix <code>x</code> is not a d-dimensional column vector
   */
  public double getProb(MatrixLib x) {
    checkHasParams();
    if (x.numCols() == 1 && x.numRows() == d) {
      return getProbInternal(x);
    }
    throw new IllegalArgumentException("The matrix given is " + x.numRows()
        + " by " + x.numCols() + " but should be a " + d + " by 1 vector.");
  }

  /**
   * Given a d-dimensional column vector x, returns the density value p =
   * 1/sqrt((2*pi)^d*|sigma|)*exp{-0.5(x-mean)'*inverse(sigma)*(x-mean)}
   */
  private double getProbInternal(MatrixLib x) {
    return Math.exp(-0.5
        * x.minus(mean).transpose().timesMat(inverseCovariance)
            .timesMat(x.minus(mean)).elementAt(0, 0))
        / normConst;
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#getLogProb(java.lang.Object)
   */
  @Override
  public double getLogProb(Object value) {
    return getLogProb((MatrixLib) value);
  }

  /**
   * Returns the natural log of the probability returned by getProb.
   */
  public double getLogProb(MatrixLib x) {
    checkHasParams();
    if (x.numCols() == 1 && x.numRows() == d) {
      return getLogProbInternal(x);
    }
    throw new IllegalArgumentException("The matrix given is " + x.numRows()
        + " by " + x.numCols() + " but should be a " + d + " by 1 vector.");
  }

  private double getLogProbInternal(MatrixLib x) {
    return ((-0.5 * x.minus(mean).transpose().timesMat(inverseCovariance)
        .timesMat(x.minus(mean)).elementAt(0, 0)) - logNormConst);
  }

  /**
   * Samples a value from this multivariate Gaussian by generating <i>d </i>
   * independent samples from univariate Gaussians with unit variance, one for
   * each mean in the mean vector, and multiplying the obtained vector on the
   * left by the square root of sigma (Cholesky decomposition of sigma). This
   * method should only be called if this distribution was constructed with a
   * fixed mean and covariance matrix (internal calls are ok if the private
   * method <code>initParams</code> is called first).
   */
  public Object sampleVal() {
    checkHasParams();
    double[][] mat = new double[d][1];
    for (int i = 0; i < d; i++) {
      mat[i][0] = UnivarGaussian.STANDARD.sampleValue();
    }
    MatrixLib temp = MatrixFactory.fromArray(mat);
    return mean.plus(sqrtCovariance.timesMat(temp));
  }

  private MatrixLib mean;
  private boolean hasMean;
  private MatrixLib covariance;
  private boolean hasCovariance;
  private boolean legalAssignment; // used to indicate that the mean and
                                   // covariance has been set up properly

  private int d;
  private double dimFactor;
  private double logDimFactor;
  private double normConst;
  private double logNormConst;
  private MatrixLib inverseCovariance;
  private MatrixLib sqrtCovariance;
}
