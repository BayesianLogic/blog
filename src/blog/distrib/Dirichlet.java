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

import blog.common.numerical.MatrixFactory;
import blog.common.numerical.MatrixLib;

/**
 * A Dirichlet distribution with shape parameter vector <code>alpha</code>,
 * defined by:
 * f(x1, x2, ... xn) = 1/Z * (x1^(alpha_1-1) * x2^(alpha_2-1) * ... *
 * xn^(alpha_n-1), where \sum(xi) = 1 and Z = \prod(Gamma(alpha_i)) /
 * Gamma(\sum(alpha_i)).
 * 
 * Note that this is a generalization of the Beta distribution: while
 * the Beta generates a parameter for the Bernoulli process (Bernoulli
 * and Binomial distributions), the Dirichlet generates parameters for
 * the Categorical process (Categorical and Multinomial distributions).
 * 
 * @since June 12, 2014
 */
public class Dirichlet implements CondProbDistrib {

  private final double TOLERANCE = 10e-3;

  /** Return the shape parameter vector <code>alpha</code>. */
  public double[] getAlpha() {
    return alpha;
  }

  /**
   * Mapping for <code>params</code>:
   * 
   * <ul>
   * <li>params[0]: <code>alpha</code> in the form of a MatrixLib instance which
   * takes on the shape of a row vector.</li>
   * </ul>
   * 
   * @see blog.distrib.CondProbDistrib#setParams(java.lang.Object[])
   */
  @Override
  public void setParams(Object[] params) {
    if (params.length != 1) {
      throw new IllegalArgumentException("expected one parameter");
    }
    setParams((MatrixLib) params[0]);
  }

  /**
   * If matrix is non-null, row-vector of strictly positive reals, sets the
   * distribution parameter <code>alpha</code> to matrix.
   * 
   * @param matrix
   *          A row vector representing the parameter vector <code>alpha</code>.
   */
  public void setParams(MatrixLib matrix) {
    if (matrix != null) {
      if (matrix.numRows() != 1 || matrix.numCols() == 0) {
        throw new IllegalArgumentException(
            "Dirichlet distribution requires nonempty vector of numbers as argument.");
      }
      if (matrix.numCols() == 1) {
        throw new IllegalArgumentException(
            "The alpha vector must contain at least two elements");
      }
      int cols = matrix.numCols();
      this.alpha = new double[cols];
      this.gammas = new Gamma[cols];
      for (int i = 0; i < cols; i++) {
        double elementValue = matrix.elementAt(0, i);
        if (elementValue <= 0) {
          throw new IllegalArgumentException(
              "All elements in the alpha vector need to be strictly positive.");
        }
        alpha[i] = elementValue;
        gammas[i] = new Gamma(elementValue, 1);
      }
      this.normalizationConstant = Dirichlet.normalize(alpha);
      this.hasAlpha = true;
    }
  }

  private void checkHasParams() {
    if (!this.hasAlpha) {
      throw new IllegalArgumentException("parameter alpha not provided");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#getProb(java.lang.Object)
   */
  @Override
  public double getProb(Object value) {
    return getProb(((MatrixLib) value));
  }

  /**
   * Returns the pdf of row vector x (of correct dimensions) in a Dirichlet
   * distribution.
   * 
   * @param x
   *          row vector
   */
  public double getProb(MatrixLib x) {
    checkHasParams();
    if (x == null) {
      throw new IllegalArgumentException("The random outcome vector is null");
    }
    if (x.numRows() != 1 || x.numCols() != this.alpha.length) {
      throw new IllegalArgumentException(
          "Incorrect dimensions given for row vector of Dirichlet. should be a 1 by "
              + alpha.length + " matrix, but is a " + x.numRows() + " by "
              + x.numCols() + " matrix");
    }
    if (!checkSupport(x)) {
      return 0.0;
    }
    double prob = 1.0;
    for (int i = 0; i < x.numCols(); i++) {
      double val = x.elementAt(0, i);
      prob *= Math.pow(val, alpha[i] - 1);
    }
    return (prob / normalizationConstant);
  }

  /**
   * Returns whether all the elements in the row vector of x sum to 1
   * Precondition: x is a row vector with # columns >= 1
   * 
   * @param x
   *          row vector
   */
  private boolean checkSupport(MatrixLib x) {
    double sum = 0.0;
    for (int i = 0; i < x.numCols(); i++) {
      sum += x.elementAt(0, i);
    }
    return (sum <= (1 + TOLERANCE)) && (sum >= (1 - TOLERANCE));
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
   * Returns the log pdf of row vector x in a Dirichlet distribution.
   * 
   * @param x
   *          row vector
   */
  public double getLogProb(MatrixLib x) {
    checkHasParams();
    if (x == null) {
      throw new IllegalArgumentException("The random outcome vector is null");
    }
    if (x.numRows() != 1 || x.numCols() != this.alpha.length) {
      throw new IllegalArgumentException(
          "Incorrect dimensions given for row vector of Dirichlet. should be a 1 by "
              + alpha.length + " matrix, but is a " + x.numRows() + " by "
              + x.numCols() + " matrix");
    }
    if (!checkSupport(x)) {
      return Double.NEGATIVE_INFINITY;
    }

    double prob = 0.0;
    for (int i = 0; i < x.numCols(); i++) {
      double value = x.elementAt(0, i);
      if (alpha[i] != 1) {
        prob += Math.log(value) * (alpha[i] - 1);
      }
    }
    return (prob - Math.log(normalizationConstant));
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#sampleVal()
   */
  @Override
  public Object sampleVal() {
    checkHasParams();
    return sampleValue();
  }

  /**
   * Samples a dirichlet distribution using the method at the following url:
   * http://en.wikipedia.org/wiki/Dirichlet_distribution#
   */
  public MatrixLib sampleValue() {
    double sum = 0.0;
    int vec_size = alpha.length;

    double[][] samples = new double[1][vec_size];
    for (int i = 0; i < vec_size; i++) {
      double sample = Gamma.sampleVal(alpha[i], 1);
      sum += sample;
      samples[0][i] = sample;
    }

    for (int i = 0; i < vec_size; i++) {
      samples[0][i] /= sum;
    }
    return MatrixFactory.fromArray(samples);
  }

  /**
   * Computes the normalization constant for a Dirichlet distribution with
   * the given parameters.
   * 
   * @param params
   *          a list of parameters of a Dirichlet distribution
   * @return the normalization constant for such a distribution
   */
  private static final double normalize(double[] params) {
    double denom = 0.0;
    double numer = 1.0;

    for (double param : params) {
      numer *= Gamma.gamma(param);
      denom += param;
    }
    denom = Gamma.gamma(denom);

    return numer / denom;
  }

  @Override
  public String toString() {
    return getClass().getName();
  }

  private double[] alpha;
  private boolean hasAlpha;
  private Gamma[] gammas;
  private double normalizationConstant;
}
