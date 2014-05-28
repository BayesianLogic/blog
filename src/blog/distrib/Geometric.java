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

import blog.*;
import blog.common.Util;

import java.util.*;

/**
 * A geometric distribution over the natural numbers 0, 1, 2,... It has a single
 * parameter alpha, which equals P(X &gt;= n+1 | X &gt;= n). Thus an alpha close
 * to 1 yields a relatively flat distribution, whereas an alpha close to 0
 * yields a distribution that decays quickly. The distribution is defined by:
 * P(X = n) = (1 - alpha) alpha^n. Its mean is alpha / (1-alpha), so to get a
 * distribution with mean m, one should use alpha = m / (1 + m).
 * 
 * <p>
 * Note that alpha cannot be 1, because then the value is infinite with
 * probability 1. However, alpha can be 0; this just means the value is 0 with
 * probability 1.
 */
public class Geometric extends AbstractCondProbDistrib {

  /**
   * Returns a Geometric distribution with the given mean.
   */
  public static Geometric constructWithMean(double mean) {
    return new Geometric(mean / (1 + mean));
  }

  /**
   * Creates a geometric distribution with the given alpha parameter.
   * 
   * @throws IllegalArgumentException
   *           if <code>alpha</code> is not in the range [0, 1)
   */
  public Geometric(double alpha) {
    alpha = 1 - alpha;
    if ((alpha < 0) || (alpha >= 1)) {
      throw new IllegalArgumentException(
          "Parameter of geometric distribution must be in the "
              + "interval [0, 1), not " + alpha);
    }

    this.alpha = alpha;
    computeLogParams();
  }

  /**
   * Creates a geometric distribution with the given alpha parameter.
   * 
   * @throws IllegalArgumentException
   *           if alpha < 0 or alpha >= 1.
   */
  public Geometric(List params) {
    if (params.size() != 1) {
      throw new IllegalArgumentException(
          "Geometric distribution requires exactly one parameter, "
              + "the success probability.");
    }

    if (!(params.get(0) instanceof Number)) {
      throw new IllegalArgumentException(
          "The first parameter (alpha) for the geometric "
              + "distribution must be of " + "class Number, not "
              + params.get(0).getClass());
    }

    alpha = 1 - ((Number) params.get(0)).doubleValue();
    if ((alpha < 0) || (alpha >= 1)) {
      throw new IllegalArgumentException(
          "Illegal alpha parameter for geometric distribution.");
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
    if (!args.isEmpty()) {
      throw new IllegalArgumentException(
          "Geometric distribution expects no arguments.");
    }

    if (!(value instanceof Integer)) {
      throw new IllegalArgumentException("The value passed to the geometric "
          + "distribution's getProb method must be " + "of class Integer, not "
          + args.get(0).getClass() + ".");
    }

    return getProb(((Integer) value).intValue());
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
    if (!args.isEmpty()) {
      throw new IllegalArgumentException(
          "Geometric distribution expects no arguments.");
    }

    if (!(value instanceof Integer)) {
      throw new IllegalArgumentException("The value passed to the geometric "
          + "distribution's getProb method must be " + "of class Integer, not "
          + args.get(0).getClass() + ".");
    }

    return getLogProb(((Integer) value).intValue());
  }

  /**
   * Generates a sample from this distribution. Expects no arguments.
   */
  public Object sampleVal(List args) {
    if (!args.isEmpty()) {
      throw new IllegalArgumentException(
          "Geometric distribution expects no arguments.");
    }

    return new Integer(sampleVal());
  }

  /**
   * Returns an integer sampled from this distribution. Uses the method from p.
   * 87 of <cite>Non-Uniform Random Variate Generation</cite> (by Luc Devroye,
   * available at http://cg.scs.carleton.ca/~luc/rnbookindex.html), which
   * exploits the fact that the geometric distribution can be seen as a
   * discretization of the exponential distribution.
   */
  public int sampleVal() {
    double u = Util.random();
    return (int) (Math.log(u) / logOneMinusAlpha);
  }

  public String toString() {
    return getClass().getName();
  }

  private void computeLogParams() {
    logAlpha = Math.log(alpha);
    logOneMinusAlpha = Math.log(1 - alpha);
  }

  /**
   * Records an occurrence of the number n, for use in updating parameters.
   */
  /*
   * public void collectStats(int n) { if (n < 0) { throw new
   * IllegalArgumentException
   * ("Geometric distribution can't generate a negative number."); }
   * 
   * count++; sum += n; }
   */

  /**
   * Sets the parameter alpha to the value that maximizes the likelihood of the
   * numbers passed to collectStats since the last call to updateParams. Then
   * clears the collected statistics, and returns the difference between the log
   * likelihood of the data under the new parameters and the log likelihood
   * under the old parameters.
   */
  /*
   * public double updateParams() { // Update parameter double oldLogProb =
   * (count * logOneMinusAlpha) + (sum * logAlpha); if (count > 0) { double mean
   * = sum / (double) count; alpha = mean / (1 + mean); cacheParams(); } double
   * newLogProb = (count * logOneMinusAlpha) + (sum * logAlpha);
   * 
   * // Clear statistics count = 0; sum = 0;
   * 
   * return (newLogProb - oldLogProb); }
   */

  private double alpha;
  private double logAlpha;
  private double logOneMinusAlpha;
}
