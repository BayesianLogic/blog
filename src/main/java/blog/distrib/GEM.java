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

import blog.common.Util;
import blog.common.numerical.MatrixFactory;
import blog.common.numerical.MatrixLib;

/**
 * A GEM distribution with a parameter <code>lambda</code> and a limitation
 * index <code>truncation</code> represents the stick lengths of a stick
 * breaking process (with parameter <code>lambda</code>) stopped after the first
 * <code>truncation</code> steps.
 * 
 * The way we generating random variables for GEM distribution with parameter
 * <code>lambda</code> and <code>truncation</code> is as follows: First, we
 * generate truncation many different random variables v_1, ...,
 * v_{truncation - 1} such that v_i ~ Beat(1, lambda), i\in\{1,...,truncation -
 * 1\}, independently. Then we calculate \pi_i=v_i\prod\limits_{j=1}{i-1}(1-v_j)
 * for i < truncation and
 * \pi_{truncation}=1-\sum\limits_{i=1}^{truncation-1}\pi_i. Finally we return
 * the vector (\pi_1,...,\pi_{truncation})^T.
 * 
 * @author Da Tang
 * @since July 29, 2014
 */
public class GEM implements CondProbDistrib {

  /**
   * set parameters for GEM distribution
   * 
   * @param params
   *          An array of one double and one integer.
   *          <ul>
   *          <li>params[0]: <code>lambda</code> A Double parameter.</li>
   *          <li>params[1]: <code>truncation</code> An integer parameter.</li>
   *          </ul>
   * 
   * @see blog.distrib.CondProbDistrib#setParams(java.lang.Object[])
   */
  @Override
  public void setParams(Object[] params) {
    if (params.length != 2) {
      throw new IllegalArgumentException("expected two parameters");
    }
    setParams((Number) params[0], (Number) params[1]);
  }

  /**
   * If both lambda and truncation are positive, set the
   * distribution parameters to them.
   * 
   * @param lambda
   *          A positive real number represents the stick-breaking parameter
   *          <code>lambda</code>.
   * 
   * @param truncation
   *          A positive integer represents the stop point of the stick-breaking
   *          process <code>truncation</code>.
   */
  public void setParams(Number lambda, Number truncation) {
    if (lambda != null) {
      if (lambda.doubleValue() <= 0) {
        throw new IllegalArgumentException(
            "GEM distribution requires a positive real number as the lambda argument.");
      }
      this.hasLambda = true;
      this.lambda = lambda.doubleValue();
    }
    if (truncation != null) {
      if (truncation.intValue() <= 0) {
        throw new IllegalArgumentException(
            "GEM distribution requires a positive integer as the truncation argument.");
      }
      this.hasTruncation = true;
      this.truncation = truncation.intValue();
    }
  }

  private void checkHasParams() {
    if (!this.hasLambda) {
      throw new IllegalArgumentException("parameter lambda not provided");
    }
    if (!this.hasTruncation) {
      throw new IllegalArgumentException("parameter truncation not provided");
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
   * check if the value is valid, i.e. it is 1 by n column vector, where n is
   * the same as truncation.
   * 
   * @param x
   * @return
   */
  private boolean checkValue(MatrixLib x) {
    if (x == null) {
      throw new IllegalArgumentException("The random outcome vector is null");
    }
    if (x.numCols() != 1 || x.numRows() != this.truncation) {
      throw new IllegalArgumentException(
          "Incorrect dimensions given for column vector of GEM. should be a "
              + truncation + " by 1 matrix, but is a " + x.numRows() + " by "
              + x.numCols() + " matrix");
    }
    return true;
  }

  /**
   * Returns the pdf of column vector x (of correct dimensions) in a GEM
   * distribution.
   * 
   * @param x
   *          column vector
   */
  public double getProb(MatrixLib x) {
    return Math.exp(getLogProb(x));
  }

  /**
   * Returns whether all the elements in the column vector of x sum to 1
   * Precondition: x is a column vector with # columns >= 1
   * 
   * @param x
   *          column vector
   */
  private boolean checkSupport(MatrixLib x) {
    double sum = 0.0;
    for (int i = 0; i < x.numRows(); i++) {
      sum += x.elementAt(i, 0);
    }
    return (sum <= (1 + Util.TOLERANCE)) && (sum >= (1 - Util.TOLERANCE));
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
   * Returns the log pdf of column vector x in a GEM distribution.
   * 
   * @param x
   *          column vector
   */
  public double getLogProb(MatrixLib x) {
    checkHasParams();
    checkValue(x);
    if (!checkSupport(x)) {
      return Double.NEGATIVE_INFINITY;
    }
    double prob = (truncation - 1) * Math.log(lambda) + (lambda - 1)
        * Math.log(x.elementAt(x.numRows() - 1, 0));
    double remain = 1.0;
    for (int i = 0; i < x.numRows() - 2; i++) {
      remain -= x.elementAt(i, 0);
      prob -= Math.log(remain);
    }
    return prob;
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#sampleVal()
   */
  @Override
  public Object sampleVal() {
    return sample_value();
  }

  /**
   * Samples a GEM distribution using the Stick-Breaking Construction.
   */
  public MatrixLib sample_value() {
    checkHasParams();

    double[][] samples = new double[truncation][1];
    double remain = 1.0;
    Beta beta = new Beta();
    beta.setParams(1, lambda);
    for (int i = 0; i < truncation - 1; i++) {
      samples[i][0] = beta.sample_value() * remain;
      remain -= samples[i][0];
    }
    samples[truncation - 1][0] = remain;
    return MatrixFactory.fromArray(samples);
  }

  @Override
  public Object[] getFiniteSupport() {
    return null;
  }

  @Override
  public String toString() {
    return "GEM(" + lambda + ", " + truncation + ")";
  }

  private double lambda;
  private int truncation;
  private boolean hasLambda;
  private boolean hasTruncation;
}
