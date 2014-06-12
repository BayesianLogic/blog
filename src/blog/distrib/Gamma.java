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
 * A Gamma distribution with shape parameter <code>k</code> and scale parameter
 * 1/<code>lambda</code>.
 * Defined as f(x) = (lambda*e^(-lambda*x)*(lambda*x)^(k - 1)) / Gamma(k) where
 * Gamma(k) = integral from 0 to infinity of t^(k-1) * e^(-t) dt
 * 
 * @since June 12, 2014
 */

public class Gamma implements CondProbDistrib {

  /** Returns the scale parameter <code>lamdba</code>. */
  public double getLambda() {
    return lambda;
  }

  /** Returns the shape parameter <code>k</code>. */
  public double getK() {
    return k;
  }

  /**
   * mapping for <code>params</code>:
   * <ul>
   * <li>params[0]: <code>k</code> (shape parameter)</li>
   * <li>params[1]: <code>lambda</code> (rate parameter)</li>
   * </ul>
   * 
   * @see blog.distrib.CondProbDistrib#setParams(java.lang.Object[])
   */
  @Override
  public void setParams(Object[] params) {
    if (params.length != 2) {
      throw new IllegalArgumentException("expected 2 parameters");
    }
    setParams((Double) params[0], (Double) params[1]);
  }

  /**
   * If the method parameter k is non-null and strictly positive, set the
   * distribution parameter <code>k</code> to the method parameter k. If the
   * method parameter lambda is non-null and strictly positive, set the
   * distribution parameter <code>lambda</code> to the method parameter lambda.
   */
  public void setParams(Double k, Double lambda) {
    if (k != null) {
      if (k <= 0) {
        throw new IllegalArgumentException(
            "parameter k (shape) must be a stricly positive real number");
      }
      this.hasK = true;
      this.k = k;
    }
    if (lambda != null) {
      if (lambda <= 0) {
        throw new IllegalArgumentException(
            "parameter lambda (rate) must be a strictly positive real number");
      }
      this.hasLambda = true;
      this.lambda = lambda;
    }
  }

  private void checkHasParams() {
    if (!this.hasK) {
      throw new IllegalArgumentException("parameter k not provided");
    }
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

  /** Returns the probability of <code>value</code>. */
  public double getProb(double value) {
    checkHasParams();
    if (value < 0) {
      return 0;
    } else {
      return (lambda * Math.exp(-lambda * value)
          * Math.pow(lambda * value, k - 1) / gamma(k));
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

  /** Returns the log probability of <code>value</code>. */
  public double getLogProb(double value) {
    return Math.log(getProb(value));
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#sampleVal()
   */
  @Override
  public Object sampleVal() {
    checkHasParams();
    return sampleVal(k, lambda);
  }

  /**
   * Should be compared with Marsiaglia's algorithm used in MATLAB.
   * Uses Cheng's rejection algorithm (GB) for k>=1,
   * rejection from Weibull distribution for 0 < k < 1.
   * 
   * @param alpha
   * @param beta
   */
  public static double sampleVal(double alpha, double beta) {
    boolean accept = false;
    if (alpha >= 1) {
      // Cheng's algorithm
      double b = (alpha - Math.log(4));
      double c = (alpha + Math.sqrt(2 * alpha - 1));
      double lam = Math.sqrt(2 * alpha - 1);
      double cheng = (1 + Math.log(4.5));
      double u, v, x, y, z, r;
      do {
        u = Util.random();
        v = Util.random();
        y = ((1 / lam) * Math.log(v / (1 - v)));
        x = (alpha * Math.exp(y));
        z = (u * v * v);
        r = (b + (c * y) - x);
        if ((r >= ((4.5 * z) - cheng)) || (r >= Math.log(z))) {
          accept = true;
        }
      } while (!accept);
      return x / beta;
    } else {
      // Weibull algorithm
      double c = (1 / alpha);
      double d = ((1 - alpha) * Math.pow(alpha, (alpha / (1 - alpha))));
      double u, v, z, e, x;
      do {
        u = Util.random();
        v = Util.random();
        z = -Math.log(u); // generating random exponential variates
        e = -Math.log(v);
        x = Math.pow(z, c);
        if ((z + e) >= (d + x)) {
          accept = true;
        }
      } while (!accept);
      return x / beta;
    }
  }

  /**
   * Returns an approximation of the Gamma function of x r(x) = integral from 0
   * to infinity of (t^(x-1) * e^(-t) dt) with |error| < 2e-10. Laczos
   * Approximation Reference: Numerical Recipes in C
   * http://www.library.cornell.edu/nr/cbookcpdf.html
   */
  public static double gamma(double x) {
    return Math.exp(lgamma(x));
  }

  /**
   * Returns an approximation of the log of the Gamma function of x. Laczos
   * Approximation Reference: Numerical Recipes in C
   * http://www.library.cornell.edu/nr/cbookcpdf.html
   */
  public static double lgamma(double x) {
    double[] cof = { 76.18009172947146, -86.50532032941677, 24.01409824083091,
        -1.231739572450155, 0.1208650973866179e-2, -0.5395239384953e-5 };
    double y, z, ser, tmp;
    y = x;
    tmp = x + 5.5;
    tmp -= ((x + 0.5) * Math.log(tmp));
    ser = 1.000000000190015;
    for (int j = 0; j < 6; j += 1) {
      y += 1;
      ser += (cof[j] / y);
    }
    return (-tmp + Math.log(2.5066282746310005 * ser / x));
  }

  @Override
  public String toString() {
    return getClass().getName();
  }

  private double lambda;
  private boolean hasLambda;
  private double k;
  private boolean hasK;

}
