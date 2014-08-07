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

import java.util.List;

/**
 * A Beta distribution with shape parameters <code>a</code> and <code>b</code>,
 * defined by f(x) =(x^(a-1)
 * * (1-x)^(b-1)) / B(a,b) where B(a,b) is a normalization constant equal to
 * integral from 0 to 1 of x^(a-1) * (1-x)^(b-1) dx
 * 
 * @since June 25, 2014
 */
public class Beta implements CondProbDistrib {

  /**
   * set parameters for beta distribution
   * 
   * @param params
   *          An array of two Numbers.
   *          <ul>
   *          <li>params[0]: <code>a</code> (Number)</li>
   *          <li>params[1]: <code>b</code> (Number)</li>
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
   * If argument a is non-null and strictly positive, then set the shape
   * parameter <code>a</code> to the method parameter a. Similarly for
   * <code>b</code>.
   */
  public void setParams(Number a, Number b) {
    if (a != null) {
      double aDouble = a.doubleValue();
      if (aDouble <= 0) {
        throw new IllegalArgumentException(
            "alpha parameter for beta distribution must be strictly positive");
      }
      this.a = aDouble;
      this.hasA = true;
      this.gammaA.setParams(aDouble, 1.0);
    }
    if (b != null) {
      double bDouble = b.doubleValue();
      if (bDouble <= 0) {
        throw new IllegalArgumentException(
            "beta parameter for beta distribution must be strictly positive");
      }
      this.b = bDouble;
      this.hasB = true;
      this.gammaB.setParams(bDouble, 1.0);
    }
  }

  private void checkHasParams() {
    if (!this.hasA) {
      throw new IllegalArgumentException("alpha parameter not provided");
    }
    if (!this.hasB) {
      throw new IllegalArgumentException("beta parameter not provided");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#getProb(java.lang.Object)
   */
  @Override
  public double getProb(Object value) {
    return getProb(((Number) value).doubleValue());
  }

  /**
   * Returns the probability of outcome x.
   */
  public double getProb(double x) {
    checkHasParams();
    if (x >= 0 && x <= 1) {
      return ((Math.pow(x, (a - 1)) * Math.pow((1 - x), (b - 1))) / beta(a, b));
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
    return getLogProb(((Number) value).doubleValue());
  }

  /**
   * Returns the log probability of outcome x.
   */
  public double getLogProb(double x) {
    checkHasParams();
    if (x >= 0 && x <= 1) {
      double t1 = 0;
      double t2 = 0;
      if (a != 1) {
        t1 = (a - 1) * Math.log(x);
      }
      if (b != 1) {
        t2 = (b - 1) * Math.log(1 - x);
      }
      return t1 + t2 - Math.log(beta(a, b));
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
    return sample_value();
  }

  public double sample_value() {
    double y = gammaA.sample_value();
    double z = gammaB.sample_value();
    return y / (y + z);
  }

  /**
   * Returns the Beta function of reals a and b B(a,b) = Gamma(a)Gamma(b) /
   * Gamma(a+b) Reference: Numerical Recipes in C
   * http://www.library.cornell.edu/nr/cbookcpdf.html
   */
  private static double beta(double a, double b) {
    return ((Gamma.gamma(a) * Gamma.gamma(b)) / Gamma.gamma(a + b));
  }

  @Override
  public String toString() {
    return "Beta(" + a + "," + b + ")";
  }

  @Override
  public List<Object> getFiniteSupport() {
    checkHasParams();
    return null;
  }

  /** Gamma(a, 1). Used for sampling. */
  private Gamma gammaA = new Gamma();
  /** Gamma(b, 1). Used for sampling. */
  private Gamma gammaB = new Gamma();

  private double a;
  private boolean hasA;
  private double b;
  private boolean hasB;
}
