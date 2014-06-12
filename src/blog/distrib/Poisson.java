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
 * A Poisson distribution with mean and variance lambda. This is a distribution
 * over non-negative integers. The probability of n is exp(-lambda) lambda^n /
 * n!.
 * 
 * This is a slightly modified version of Poisson.java in the common directory,
 * tailored to implement the CondProbDistrib interface.
 * 
 * @since June 12, 2014
 */
public class Poisson implements CondProbDistrib {

  /**
   * Computes the log probability of <code>n</code> for a Poisson with parameter
   * <code>lambda</code>.
   */
  public static double computeLogProb(double lambda, int n) {
    if (lambda == 0) {
      return n == 0 ? 0 : Double.NEGATIVE_INFINITY;
    }
    return (-lambda + (n * Math.log(lambda)) - Util.logFactorial(n));
  }

  /**
   * Returns an integer sampled according to this distribution. This
   * implementation takes time proportional to the magnitude of the integer
   * returned. I got the algorithm from Anuj Kumar's course page for IEOR E4404
   * at Columbia University, specifically the file: <blockquote>
   * http://www.columbia.edu/~ak2108/ta/summer2003/poisson1.c </blockquote>
   */
  public int sampleInt() {
    return sampleVal(lambda);
  }

  /**
   * sample from Poisson distribution when the parameter lambda is small (< 15)
   * 
   * @param lambda
   * @return
   */
  private static int sampleSmall(double lambda) {
    int n = 0;
    double probOfN = Math.exp(-lambda); // start with prob of 0
    double cumProb = probOfN;

    double u = Util.random();

    while (cumProb < u) {
      n++;
      // ratio between P(n) and P(n-1) is lambda / n
      probOfN *= (lambda / n);
      cumProb += probOfN;
    }

    return n;
  }

  /**
   * Naive inverse CDF approach for Poisson is not advisable for lambda > 15.
   * Here we are following MATLAB's implementation, where gamma random variables
   * are subtracted from lambda until it falls into the range of lambda <= 15.
   * Then, standard naive inverse CDF sampling is applied via sampleSmall.
   * 
   * @param lambda
   * @return
   */
  public static int sampleVal(double lambda) {
    if (lambda < 15)
      return sampleSmall(lambda);

    double alpha = 7.0 / 8.0;
    int m = (int) Math.floor(alpha * lambda);
    double x = Gamma.sampleVal(m, 1);
    int r;
    if (x < lambda) {
      r = m + sampleVal(lambda - x);
    } else {
      r = Binomial.sampleVal(m - 1, lambda / x);
    }
    return r;
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
      w += Math.exp(computeLogProb(lambda, i));
    }
    return w;
  }

  /** Return the parameter <code>lambda</code>. */
  public double getLambda() {
    return lambda;
  }

  /**
   * mapping for <code>params</code>:
   * <ul>
   * <li>params[0]: <code>lambda</code></li>
   * </ul>
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
   */
  public void setParams(Double lambda) {
    if (lambda != null) {
      if (lambda < 0) {
        throw new IllegalArgumentException(
            "parameter lambda must be a nonnegative real");
      }
      this.lambda = lambda;
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
   * Returns the probability of k occurrences in a Poisson distribution.
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
   * Returns the log probability of k occurrences in a Poisson distribution
   * 
   * @param k
   *          # occurrences
   */
  public double getLogProb(int k) {
    checkHasParams();
    if (lambda == 0) {
      return k == 0 ? 0 : Double.NEGATIVE_INFINITY;
    }
    return (-lambda + (k * Math.log(lambda)) - Util.logFactorial(k));
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#sampleVal()
   */
  @Override
  public Object sampleVal() {
    checkHasParams();
    return sampleInt();
  }

  @Override
  public String toString() {
    return getClass().getName();
  }

  private double lambda;
  private boolean hasLambda;
}
