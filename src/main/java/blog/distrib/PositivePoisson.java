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

/**
 * A Poisson distribution with mean lambda and variance (lambda - 1). This is a
 * distribution over positive integers. The probability of n is exp(-(lambda -
 * 1)) lambda ^ (n - 1) / (n - 1)!.
 * 
 * @since July 14, 2014
 */
public class PositivePoisson implements CondProbDistrib {

  public PositivePoisson() {
    poiss = new Poisson();
  }

  /**
   * Computes the log probability of <code>n</code> for a Poisson with parameter
   * <code>lambda</code>.
   */
  public static double computeLogProb(double lambda, int n) {
    return Poisson.computeLogProb(lambda - 1, n - 1);
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
    return poiss.cdf(a - 1, b - 1);
  }

  public static double cdf(double lambda, int a, int b) {
    return Poisson.cdf(lambda - 1, a - 1, b - 1);
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
      poiss.setParams((Number) (this.lambda - 1));
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
    return poiss.getProb(k - 1);
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
    return poiss.getLogProb(k - 1);
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
    return (poiss.sample_value() + 1);
  }

  /**
   * Returns an integer sampled according to the Poisson distribution. This
   * implementation takes time proportional to the magnitude of the integer
   * returned.
   */
  public static int sample_value(double lambda) {
    return Poisson.sample_value(lambda - 1) + 1;
  }

  @Override
  public String toString() {
    return "PositivePoisson(" + lambda + ")";
  }

  private Poisson poiss;
  private double lambda;
}
