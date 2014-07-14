/*
 * Copyright (c) 2005, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the University of California, Berkeley nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior
 * written permission.
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
 * A Poisson distribution with mean lambda and variance (lambda - 1). This is a
 * distribution over positive integers. The probability of n is exp(-(lambda -
 * 1)) lambda ^ (n - 1) / (n - 1)!.
 * 
 * @since July 14, 2014
 */
public class PositivePoisson implements CondProbDistrib {
  /**
   * Computes the log probability of <code>n</code> for a Poisson with parameter
   * <code>lambda</code>.
   */
  public static double computeLogProb(double lambda, int n) {
    if (lambda == 1) {
      return n == 1 ? 0 : Double.NEGATIVE_INFINITY;
    }
    return (-(lambda - 1) + ((n - 1) * Math.log(lambda - 1)) - Util
        .logFactorial(n - 1));
  }

  /**
   * sample from Positive Poisson distribution when the parameter lambda is
   * small (< 16)
   * 
   * @param lambda
   * @return
   */
  private static int sampleSmall(double lambda) {
    int n = 0;
    double probOfN = Math.exp(-(lambda - 1)); // start with prob of 0
    double cumProb = probOfN;

    double u = Util.random();

    while (cumProb < u) {
      n++;
      // ratio between P(n) and P(n-1) is (lambda - 1) / n
      probOfN *= ((lambda - 1) / n);
      cumProb += probOfN;
    }

    return (n + 1);
  }

  private double[] cdf_table = null;

  private static double[] ensureSize(int n, double[] table) {
    double[] tb = table;
    if (table == null)
      tb = new double[n + 1];
    else if (n >= table.length) {
      tb = new double[n + 1];
      System.arraycopy(table, 0, tb, 0, table.length);
    }
    return tb;
  }

  private void ensureCDFTable(int n) {
    int oldn = 0;
    double w;
    if (cdf_table != null) {
      oldn = cdf_table.length;
    }
    if ((cdf_table == null) || (n >= cdf_table.length)) {
      cdf_table = ensureSize(n, cdf_table);
    }
    if (oldn > 0)
      w = cdf_table[oldn - 1];
    else
      w = 0;
    for (; oldn < cdf_table.length; oldn++) {
      if (oldn > 0)
        w += Math.exp(computeLogProb(lambda, oldn));
      cdf_table[oldn] = w;
    }
  }

  /**
   * dynamically construct cdf table based on demand, and return cdf within the
   * region of a to b, inclusive
   * 
   * @param lambda
   * @param a
   * @param b
   * @return
   */
  public double cdf(int a, int b) {
    ensureCDFTable(b);

    if (a <= 0)
      return cdf_table[b];
    else
      return cdf_table[b] - cdf_table[a - 1];
  }

  public static double cdf(double lambda, int a, int b) {
    double w = 0;
    for (int i = a; i <= b; i++) {
      if (i > 0)
        w += Math.exp(computeLogProb(lambda, i));
    }
    return w;
  }

  /**
   * set parameters for Positive Poisson distribution
   * 
   * @param params
   *          array of the form [Number]
   *          <ul>
   *          <li>params[0]: <code>lambda</code> (Number)</li>
   *          </ul>
   * 
   * @see blog.distrib.CondProbDistrib#setParams(java.lang.Object[])
   */
  @Override
  public void setParams(Object[] params) {
    if (params.length != 1) {
      throw new IllegalArgumentException("expected one parameter");
    }
    setParams((Number) params[0]);
  }

  /**
   * If the method parameter lambda is non-null and non-negative, set the
   * distribution parameter <code>lambda</code> to the method parameter lambda.
   */
  public void setParams(Number lambda) {
    if (lambda != null) {
      if (lambda.doubleValue() < 0) {
        throw new IllegalArgumentException(
            "parameter lambda must be a nonnegative real");
      }
      this.lambda = lambda.doubleValue();
      this.hasLambda = true;
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
    return getProb(((Integer) value).intValue());
  }

  /**
   * Returns the probability of k occurrences in a Positive Poisson
   * distribution.
   * 
   * @param k
   *          # occurrences
   */
  public double getProb(int k) {
    return Math.exp(getLogProb(k));
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

  /**
   * Returns the log probability of k occurrences in a Positive Poisson
   * distribution
   * 
   * @param k
   *          # occurrences
   */
  public double getLogProb(int k) {
    checkHasParams();
    if (lambda == 1) {
      return k == 1 ? 0 : Double.NEGATIVE_INFINITY;
    }
    return (-(lambda - 1) + ((k - 1) * Math.log(lambda - 1)) - Util
        .logFactorial(k - 1));
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

  /** Samples from the Poisson distribution. */
  public int sample_value() {
    checkHasParams();
    return sample_value(lambda);
  }

  /**
   * Returns an integer sampled according to the Poisson distribution. This
   * implementation takes time proportional to the magnitude of the integer
   * returned.
   */
  public static int sample_value(double lambda) {
    if (lambda < 16)
      return sampleSmall(lambda);
    double alpha = 7.0 / 8.0;
    int m = (int) Math.floor(alpha * (lambda - 1));
    double x = Gamma.sample_value(m, 1);
    int r;
    if (x < lambda - 1) {
      r = m + Poisson.sample_value(lambda - 1 - x);
    } else {
      r = Binomial.sample_value(m - 1, (lambda - 1) / x);
    }
    return r + 1;
  }

  @Override
  public String toString() {
    return "PositivePoisson(" + lambda + ")";
  }

  private double lambda;
  private boolean hasLambda;
}
