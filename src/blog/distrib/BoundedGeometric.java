/*
 * Copyright (c) 2006, Regents of the University of California
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

import blog.common.Util;

/**
 * Like a geometric distribution, but with an upper bound <i>B</i>. The value
 * <i>B</i> gets all the probability mass that would ordinarily go to numbers
 * greater than or equal to <i>B</i>. The parameter alpha now denotes P(X &gt;=
 * n+1 | X &gt;= n) only for n &lt; <i>B</i>. The distribution is defined by:
 * 
 * <pre>
 *   P(X = n) = (1 - alpha) alpha^n  for n < B
 *   P(X = B) = alpha^B
 * </pre>
 * 
 * The alpha value should be given as a parameter, and the upper bound as an
 * argument.
 */
public class BoundedGeometric extends AbstractCondProbDistrib {
  /**
   * Creates a bounded geometric distribution with the given alpha parameter.
   * 
   * @throws IllegalArgumentException
   *           if alpha < 0 or alpha > 1.
   */
  public BoundedGeometric(List params) {
    if (params.size() != 1) {
      throw new IllegalArgumentException(
          "BoundedGeometric distribution requires exactly one "
              + "parameter, the success probability.");
    }

    if (!(params.get(0) instanceof Number)) {
      throw new IllegalArgumentException(
          "The first parameter (alpha) for the bounded geometric "
              + "distribution must be of " + "class Number, not "
              + params.get(0).getClass());
    }

    alpha = ((Number) params.get(0)).doubleValue();
    if ((alpha < 0) || (alpha > 1)) {
      throw new IllegalArgumentException(
          "Illegal alpha parameter for bounded geometric " + "distribution: "
              + alpha);
    }

    computeLogParams();
  }

  /**
   * Returns the probability of the given integer under this distribution.
   */
  public double getProb(int n) {
    if (n < 0) {
      return 0;
    }
    return (1 - alpha) * Math.pow(alpha, n);
  }

  /**
   * Returns the probability of the given value, which should be an Integer.
   * Expects no arguments.
   */
  public double getProb(List args, Object value) {
    int bound = processArgs(args);

    if (!(value instanceof Integer)) {
      throw new IllegalArgumentException("The value passed to the geometric "
          + "distribution's getProb method must be " + "of class Integer, not "
          + args.get(0).getClass() + ".");
    }
    int n = ((Integer) value).intValue();

    if ((n >= 0) && (n < bound)) {
      return (1 - alpha) * Math.pow(alpha, n);
    }
    if (n == bound) {
      return Math.pow(alpha, bound);
    }
    return 0;
  }

  /**
   * Returns the natural log of the probability of the given integer under this
   * distribution.
   */
  public double getLogProb(int n) {
    if (n < 0) {
      return Double.NEGATIVE_INFINITY;
    }

    // log of (1 - alpha) * (alpha ^ n)
    return logOneMinusAlpha + (n * logAlpha);
  }

  /**
   * Returns the log probability of the given value, which should be an Integer.
   * Expects no arguments.
   */
  public double getLogProb(List args, Object value) {
    int bound = processArgs(args);

    if (!(value instanceof Integer)) {
      throw new IllegalArgumentException("The value passed to the geometric "
          + "distribution's getProb method must be " + "of class Integer, not "
          + args.get(0).getClass() + ".");
    }
    int n = ((Integer) value).intValue();

    if ((n >= 0) && (n < bound)) {
      // log of (1 - alpha) * (alpha ^ n)
      return logOneMinusAlpha + (n * logAlpha);
    }
    if (n == bound) {
      return bound * logAlpha;
    }
    return Double.NEGATIVE_INFINITY;
  }

  /**
   * Generates a sample from this distribution. Expects no arguments.
   */
  public Object sampleVal(List args) {
    int bound = processArgs(args);

    double u = Util.random();
    double cumProb = 0;

    int n = 0;
    double p = 1; // probability that value is >= n
    while (true) {
      if (n == bound) {
        break;
      }

      cumProb += ((1 - alpha) * p);
      if (cumProb > u) {
        break;
      }

      ++n;
      p *= alpha;
    }

    return new Integer(n);
  }

  public String toString() {
    return getClass().getName();
  }

  private void computeLogParams() {
    logAlpha = Math.log(alpha);
    logOneMinusAlpha = Math.log(1 - alpha);
  }

  private int processArgs(List args) {
    if ((args.size() != 1) || !(args.get(0) instanceof Integer)) {
      throw new IllegalArgumentException(
          "BoundedGeometric CPD takes one argument, the upper "
              + "bound on possible values (got: " + args + ")");
    }

    int bound = ((Integer) args.get(0)).intValue();
    if (bound < 0) {
      throw new IllegalArgumentException(
          "Illegal upper bound for BoundedGeometric: " + bound);
    }

    return bound;
  }

  private double alpha;
  private double logAlpha;
  private double logOneMinusAlpha;

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#setParams(java.util.List)
   */
  @Override
  public void setParams(Object[] params) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#getProb(java.lang.Object)
   */
  @Override
  public double getProb(Object value) {
    // TODO Auto-generated method stub
    return 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#getLogProb(java.lang.Object)
   */
  @Override
  public double getLogProb(Object value) {
    // TODO Auto-generated method stub
    return 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#sampleVal()
   */
  @Override
  public Object sampleVal() {
    // TODO Auto-generated method stub
    return null;
  }
}
