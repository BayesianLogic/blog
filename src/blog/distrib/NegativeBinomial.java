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
 * A Negative Binomial distribution with parameters <code>r</code> (number of
 * failures) and <code>p</code> (probability of a success at a given trial). If
 * <code>r</code> is an integer, this is also called the Pascal distribution.
 * The distribution is defined (in discrete terms) as the number of successes
 * before <code>r</code> failures. The distribution follows the wikipedia
 * definition of the Negative Binomial distribution.
 * 
 * @since June 17, 2014
 */
public class NegativeBinomial implements CondProbDistrib {

  /**
   * set parameters for Negative Binomial distribution.
   * 
   * @param params
   *          array of the form [Integer, Double]
   *          <ul>
   *          <li>params[0]: <code>r</code>, number of failures (Integer)</li>
   *          <li>params[1]: <code>p</code>, probability of succes at a given
   *          trial (Double)</li>
   *          </ul>
   * 
   * @see blog.distrib.CondProbDistrib#setParams(java.lang.Object[])
   */
  @Override
  public void setParams(Object[] params) {
    if (params.length != 2) {
      throw new IllegalArgumentException("expected 2 parameters");
    }
    setParams((Integer) params[0], (Double) params[1]);
  }

  /**
   * If the method parameter r is non-null and strictly positive, set the
   * distribution parameter <code>r</code> to method parameter r. If the method
   * parameter p is non-null and 0 < p < 1, set the distribution parameter
   * <code>p</code> to the method parameter p.
   * 
   * @param r
   *          number of failures, r > 0
   * @param p
   *          success probability, 0 < p < 1
   */
  public void setParams(Integer r, Double p) {
    if (r != null) {
      if (r <= 0) {
        throw new IllegalArgumentException(
            "parameter r must be a strictly positive integer");
      }
      this.hasR = true;
      this.r = r;
    }
    if (p != null) {
      if (p <= 0 || p >= 1) {
        throw new IllegalArgumentException(
            "parameter p must be in the interval (0, 1) not " + p + ".");
      }
      this.hasP = true;
      this.p = p;
    }
    if (this.hasP && this.hasR) {
      gamma = new Gamma(this.r, (this.p / (1 - this.p)));
    }
  }

  private void checkHasParams() {
    if (!this.hasR) {
      throw new IllegalArgumentException("parameter r not provided");
    }
    if (!this.hasP) {
      throw new IllegalArgumentException("parameter p not provided");
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
   * Returns the probability of k successes before r failures
   * 
   * @param k
   *          number of successes
   */
  public double getProb(int k) {
    return Math.exp(getLogProb(k));
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
   * Returns the log probability of k successes before r failures
   * 
   * @param k
   *          number of successes
   */
  public double getLogProb(int k) {
    checkHasParams();
    if (k < 0) {
      return Double.NEGATIVE_INFINITY;
    }
    return (k * Math.log(p) + r * Math.log(1 - p)
        + Util.logFactorial(k + r - 1) - Util.logFactorial(k) - Util
          .logFactorial(r - 1));
  }

  /**
   * Returns a double sampled according to this distribution. Takes time
   * O(GammaDistrib.sampleVal() + Poisson.sampleVal()). (Reference: A Guide To
   * Simulation, 2nd Ed. Bratley, Paul, Bennett L. Fox and Linus E. Schrage.)
   * 
   * @see blog.distrib.CondProbDistrib#sampleVal()
   */
  @Override
  public Object sampleVal() {
    return sample_value();
  }

  /** Samples from the Negative Binomial distribution. */
  public int sample_value() {
    // Need to fix this to call instance methods sample_value of theta and
    // poisson
    checkHasParams();
    Double theta = (Double) gamma.sampleVal();
    return Poisson.sampleVal(theta);
  }

  @Override
  public String toString() {
    return getClass().getName();
  }

  private Gamma gamma;
  private int r;
  private boolean hasR;
  private double p;
  private boolean hasP;
}
