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
 * A Binomial distribution with parameters <code>n</code> (number of trials) and
 * <code>p</code> (probability of success for a given trial). The probability of
 * k successes
 * P(k)= C(n,k) * p^k * (1-p)^(n-k).
 * 
 * @since June 17, 2014
 */

public class Binomial implements CondProbDistrib {

  /**
   * set parameters for binomial distribution
   * 
   * @param params
   *          An array of the form: [Integer, Double]
   *          <ul>
   *          <li>params[0]: <code>n</code> (Integer)</li>
   *          <li>params[1]: <code>p</code> (Double)</li>
   *          </ul>
   * 
   * @see blog.distrib.CondProbDistrib#setParams(java.lang.Object[])
   */
  @Override
  public void setParams(Object[] params) {
    if (params.length != 2) {
      throw new IllegalArgumentException("expected two parameters");
    }
    setParams((Integer) params[0], (Double) params[1]);
  }

  /**
   * If the method parameter n is non-null and legal, set the distribution
   * parameter <code>n</code> to the method parameter. Similarly for p.
   * 
   * @param n
   *          <code>n</code> (number of trials); to be legal, must be a
   *          nonnegative integer
   * @param p
   *          <code>p</code> (probability of success); to be legal, must be a
   *          probability (0 <= p <= 1)
   */
  public void setParams(Integer n, Double p) {
    if (n != null) {
      if (n < 0) {
        throw new IllegalArgumentException(
            "parameter n for a binomial distribution must be a nonnegative intger");
      }
      this.n = n;
      this.hasN = true;
    }
    if (p != null) {
      if (p < 0 || p > 1) {
        throw new IllegalArgumentException(
            "parameter p for a binomial distribution must be a probability in the interval [0, 1]");
      }
      this.p = p;
      this.hasP = true;
    }
  }

  private void checkHasParams() {
    if (!this.hasP) {
      throw new IllegalArgumentException("parameter p not provided");
    }
    if (!this.hasN) {
      throw new IllegalArgumentException("parameter n not provided");
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

  /** Returns the probability of <code>value</code> successes. */
  public double getProb(int value) {
    checkHasParams();
    int k = value;
    if (k >= 0 && k <= n) {
      return ((Util.factorial(n) / (Util.factorial(k) * Util.factorial(n - k)))
          * Math.pow(p, k) * Math.pow((1 - p), (n - k)));
    } else {
      return 0;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#getLogProb(java.lang.Object)
   */
  @Override
  public double getLogProb(Object value) {
    return getLogProb(((Integer) value).intValue());
  }

  /** Returns the log probability of <code>value</code> successes. */
  public double getLogProb(int value) {
    checkHasParams();
    int k = value;
    if (k >= 0 && k <= n) {
      return Util.logPartialFactorial(n, k) - Util.logFactorial(k) + k
          * Math.log(p) + (n - k) * Math.log(1 - p);
    } else {
      return Double.NEGATIVE_INFINITY;
    }
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
   * Samples the current binomial distribution.
   */
  public int sample_value() {
    double q = -Math.log(1 - p);
    double sum = 0;
    int x = 0;
    double e;
    while (sum <= q) {
      e = Exponential.sampleVal(1);
      sum += (e / (n - x));
      x += 1;
    }
    return x - 1;
  }

  @Override
  public String toString() {
    return getClass().getName();
  }

  private int n;
  private boolean hasN;
  private double p;
  private boolean hasP;
}
