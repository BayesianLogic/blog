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
 * A Negative Binomial distribution with parameters r (number of failures) and
 * p (probability of a success at a given trial). If r is an integer, this is
 * also called the Pascal distribution. The distribution is defined (in discrete
 * terms) as the number of successes before r failures. The distribution follows
 * the wikipedia definition of the Negative Binomial distribution.
 */
public class NegativeBinomial extends AbstractCondProbDistrib {

  public int getR() {
    return r;
  }

  public double getP() {
    return p;
  }

  @Override
  /**
   * params[0] -> r, according to class definition
   * params[1] -> p, according to class definition
   */
  public void setParams(Object[] params) {
    if (params.length != 2) {
      throw new IllegalArgumentException("expected 2 parameters");
    }
    setParams((Integer) params[0], (Double) params[1]);
  }

  /**
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

  @Override
  /**
   * Returns a double sampled according to this distribution. Takes time
   * O(GammaDistrib.sampleVal() + Poisson.sampleVal()). (Reference: A Guide To
   * Simulation, 2nd Ed. Bratley, Paul, Bennett L. Fox and Linus E. Schrage.)
   */
  public Object sampleVal() {
    Double theta = (Double) gamma.sampleVal();
    return Poisson.sampleVal(theta);
  }

  public String toString() {
    return getClass().getName();
  }

  private Gamma gamma;
  private int r;
  private boolean hasR;
  private double p;
  private boolean hasP;
}
