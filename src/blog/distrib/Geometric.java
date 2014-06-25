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
 * A geometric distribution over the natural numbers 0, 1, 2,... It has a single
 * parameter <code>alpha</code>, which equals the probability of a success
 * trial. Thus an <code>alpha</code> close to 0 yields a relatively flat
 * distribution, whereas an <code>alpha</code> close to 1 yields a distribution
 * that decays quickly. The distribution is defined by: P(Y = n) = (1 - alpha)^n
 * alpha. Its mean is (1-alpha) / alpha, so
 * to get a distribution with mean m, one should use alpha = 1 / (1 + m).
 * 
 * <p>
 * Note that <code>alpha</code> cannot be 0, because then the value is infinite
 * with probability 1. However, alpha can be 1; this just means the value is 0
 * with probability 1.
 * 
 * @since June 17, 2014
 */
public class Geometric implements CondProbDistrib {

  /**
   * set parameters for Geometric distribution
   * 
   * @param params
   *          An array of the form [Double]
   *          <ul>
   *          <li>params[0]: <code>alpha</code>(Double)</li>
   *          </ul>
   * 
   * @see blog.distrib.CondProbDistrib#setParams(java.lang.Object[])
   */
  @Override
  public void setParams(Object[] params) {
    if (params.length != 1) {
      throw new IllegalArgumentException("expected 1 parameter");
    }
    setParams((Double) params[0]);
  }

  /**
   * If method parameter alpha is non-null and 0 < alpha <= 1, then set
   * distribution parameter <code>alpha</code> to method parameter alpha.
   */
  public void setParams(Double alpha) {
    if (alpha != null) {
      if (alpha <= 0 || alpha > 1) {
        throw new IllegalArgumentException(
            "Parameter of geometric distribution must be in the "
                + "interval (0, 1], not " + alpha);
      }
      this.alpha = alpha;
      this.hasAlpha = true;
      computeLogParams();
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
    return getProb(((Integer) value).intValue());
  }

  /**
   * Returns the probability of the given integer under this distribution.
   */
  public double getProb(int k) {
    checkHasParams();
    if (k < 0) {
      return 0;
    }
    return alpha * Math.pow(1 - alpha, k);
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.AbstractCondProbDistrib#getLogProb(java.lang.Object)
   */
  @Override
  public double getLogProb(Object value) {
    return getLogProb(((Integer) value).intValue());
  }

  /**
   * Returns the log probability of the given integer under this distribution.
   */
  public double getLogProb(int k) {
    checkHasParams();
    if (k < 0) {
      return Double.NEGATIVE_INFINITY;
    }

    // log of alpha * (1 - alpha) ^ k
    // Special Case: Otherwise, k * log(1-alpha) is NaN in Java
    if (k == 0 && alpha == 1) {
      return 0.0;
    }
    return logAlpha + (k * logOneMinusAlpha);
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#sampleVal()
   */
  @Override
  public Object sampleVal() {
    checkHasParams();
    return sample_value();
  }

  /**
   * Returns an integer sampled from this distribution. Uses the method from p.
   * 87 of <cite>Non-Uniform Random Variate Generation</cite> (by Luc Devroye,
   * available at http://cg.scs.carleton.ca/~luc/rnbookindex.html), which
   * exploits the fact that the geometric distribution can be seen as a
   * discretization of the exponential distribution.
   */
  public int sample_value() {
    double u = Util.random();
    return (int) (Math.log(u) / logOneMinusAlpha);
  }

  @Override
  public String toString() {
    return "Geometric(" + alpha + " )";
  }

  private void computeLogParams() {
    logAlpha = Math.log(alpha);
    logOneMinusAlpha = Math.log(1 - alpha);
  }

  private double alpha;
  private boolean hasAlpha;
  private double logAlpha;
  private double logOneMinusAlpha;
}
