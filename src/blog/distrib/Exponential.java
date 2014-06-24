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

import blog.common.Util;

/**
 * An Exponential distribution with parameter <code>lambda</code> over
 * non-negative reals.
 * The probability of x is lambda * e^(-lambda*x).
 * 
 * @since June 17, 2014
 */

public class Exponential implements CondProbDistrib {

  /**
   * set parameters for Exponential distribution
   * 
   * @param params
   *          An array of the form [Double]
   *          <ul>
   *          <li>params[0]: <code>lambda</code> (Double)</li>
   *          </ul>
   * 
   * @see blog.distrib.CondProbDistrib#setParams(java.lang.Object[])
   */
  @Override
  public void setParams(Object[] params) {
    if (params.length != 1) {
      throw new IllegalArgumentException("expected one parameter");
    }
    setParams((Double) params[0]);
  }

  /**
   * If the method parameter lambda is non-null and non-negative, set the
   * distribution parameter <code>lambda</code> to the method parameter lambda.
   * 
   */
  public void setParams(Double lambda) {
    if (lambda != null) {
      if (lambda <= 0) {
        throw new IllegalArgumentException(
            "parameter lambda for an exponential distribution must be a strictly positive real");
      }
      this.hasLambda = true;
      this.lambda = lambda;
      this.logLambda = Math.log(lambda);
    }
  }

  private void checkHasParams() {
    if (!this.hasLambda) {
      throw new IllegalArgumentException("parameter lambda not provided");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#getProb(java.lang.Object)
   */
  @Override
  public double getProb(Object value) {
    return getProb(((Double) value).doubleValue());
  }

  /** Return the probability of <code>value</code>. */
  public double getProb(double value) {
    checkHasParams();
    if (value < 0) {
      return 0;
    } else {
      return (lambda * Math.exp((-lambda) * value));
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#getLogProb(java.lang.Object)
   */
  @Override
  public double getLogProb(Object value) {
    return getLogProb(((Double) value).doubleValue());
  }

  /** Return the log probability of <code>value</code>. */
  public double getLogProb(double value) {
    checkHasParams();
    if (value < 0) {
      return Double.NEGATIVE_INFINITY;
    } else {
      return logLambda - lambda * value;
    }
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

  /** Samples a value from an exponential distribution. */
  public double sample_value() {
    checkHasParams();
    return -Math.log(Util.random()) / this.lambda;
  }

  /**
   * Generate a value from Exponential distribution with parameter
   * <code>lambda</code>.
   */
  public static double sample_value(double lambda) {
    return -Math.log(Util.random()) / lambda;
  }

  @Override
  public String toString() {
    return "Exponential(" + lambda + ")";
  }

  private boolean hasLambda;
  private double lambda;
  private double logLambda;
}
