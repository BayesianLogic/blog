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
import blog.model.Type;

import java.util.*;

/**
 * Laplace distribution over real numbers. This CPD can be initialized
 * with zero, one, or two parameters. If two parameters are given, then the
 * first is the mean and the second is the diversity. If only one parameter is
 * given, it is interpreted as the diversity, and the <code>getProb</code> and
 * <code>sampleVal</code> methods will expect one argument specifying the mean.
 * If no parameters are given, then those methods will expect two arguments, the
 * mean and the diversity.
 */
public class Laplace extends AbstractCondProbDistrib {
  public static final Laplace STANDARD = new Laplace(0, 1);

  /**
   * Creates a univariate Laplace distribution with the given fixed mean and
   * variance.
   */
  public Laplace(double mean, double variance) {
    fixedMean = true;
    setMean(new Double(mean));

    fixedDiversity = true;
    setDiversity(new Double(variance));
  }

  /**
   * Creates a univariate Laplace distribution. If two parameters are given,
   * then the first is the mean and the second is the variance. If only one
   * parameter is given, it is interpreted as the variance. Parameters not
   * specified here must be given as arguments to <code>getProb</code> and
   * <code>sampleVal</code>.
   */
  public Laplace(List params) {
    if (params.size() == 0) {
      fixedMean = false;
      fixedDiversity = false;
    } else if (params.size() == 1) {
      fixedMean = false;
      fixedDiversity = true;
      setDiversity(params.get(0));
    } else if (params.size() == 2) {
      fixedMean = true;
      setMean(params.get(0));
      fixedDiversity = true;
      setDiversity(params.get(1));
    } else {
      throw new IllegalArgumentException(
          "UnivarLaplace CPD expects at most 2 parameters, not "
              + params.size());
    }
  }

  public double getProb(List args, Object value) {
    initParams(args);
    if (!(value instanceof Number))
      throw new IllegalArgumentException(
          "The value passed to the univariate Laplace distribution's "
              + "getProb " + "method must be of type Number, not "
              + value.getClass() + ".");
    return getProb(((Number) value).doubleValue());
  }

  public double getLogProb(List args, Object value) {
    initParams(args);
    if (!(value instanceof Number))
      throw new IllegalArgumentException(
          "The value passed to the univariate Laplace distribution's "
              + "getLogProb " + "method must be of type Number, not "
              + value.getClass() + ".");
    return getLogProb(((Number) value).doubleValue());
  }

  /**
   * Returns the density of this Gaussian distribution at the given value. This
   * method should only be called if the mean and variance were set in the
   * constructor (internal calls are ok if the private method
   * <code>initParams</code> is called first).
   */
  public double getProb(double x) {
    return (Math.exp(-Math.abs(x - mu) / b) / normConst);
  }

  /**
   * Returns the natural log of the density of this Laplace distribution at the
   * given value. This method should only be called if the mean and diversity
   * were set in the constructor, or if <code>initParams</code> has been called.
   */
  public double getLogProb(double x) {
    return (-Math.abs(x - mu) / b) - logNormConst;
  }

  public Object sampleVal(List args, Type childType) {
    initParams(args);
    return new Double(sampleVal());
  }

  /**
   * Returns a value sampled from this Laplace distribution. This method should
   * only be called if the mean and diversity were set in the constructor
   * (internal calls are ok if the private method <code>initParams</code> is
   * called first).
   * 
   * <p>
   * The implementation treats a Laplace distribution as the combination of two
   * Exponential distributions. See
   * http://en.wikipedia.org/wiki/Laplace_distribution
   * #Generating_random_variables_according_to_the_Laplace_distribution
   */
  public double sampleVal() {
    double U = Util.random() - 0.5;
    if (U > 0)
      return mu - b * Math.log(1 - 2 * U);
    else
      return mu + b * Math.log(1 + 2 * U);
  }

  private void initParams(List args) {
    if (fixedMean) {
      if (args.size() > 0) {
        throw new IllegalArgumentException(
            "UnivarLaplace CPD with fixed mean expects no " + "arguments.");
      }
    } else {
      if (args.size() < 1) {
        throw new IllegalArgumentException(
            "UnivarLaplace CPD created without a fixed mean; "
                + "requires mean as an argument.");
      }
      setMean(args.get(0));

      if (fixedDiversity) {
        if (args.size() > 1) {
          throw new IllegalArgumentException(
              "UnivarLaplace CPD with fixed variance expects "
                  + "only one argument.");
        }
      } else {
        if (args.size() < 2) {
          throw new IllegalArgumentException(
              "UnivarLaplace CPD created without a fixed "
                  + "variance; requires variance as argument.");
        }
        setDiversity(args.get(1));
      }
    }
  }

  private void setMean(Object mean) {
    if (!(mean instanceof Number)) {
      throw new IllegalArgumentException(
          "Mean of UnivarLaplace distribution must be a number, " + "not "
              + mean + " of " + mean.getClass());
    }
    mu = ((Number) mean).doubleValue();
  }

  public double getMean() {
    return mu;
  }

  public double getDiversity() {
    return b;
  }

  public double getVariance() {
    return 2 * b * b;
  }

  public double getDeviation() {
    return Math.sqrt(2) * b;
  }

  private void setDiversity(Object diversity) {
    if (!(diversity instanceof Number)) {
      throw new IllegalArgumentException(
          "Diversity of UnivarLaplace distribution must be a number, " + "not "
              + diversity + " of " + diversity.getClass());
    }
    b = ((Number) diversity).doubleValue();

    if (b <= 0) {
      throw new IllegalArgumentException(
          "Diversity of UnivarLaplace distribution must be positive, " + "not "
              + b);
    }
    normConst = b * 2;
    logNormConst = Math.log(b) + Math.log(2);
  }

  /**
   * Returns a Laplace representing the posterior of the mean of this Laplace
   * (but ignoring its currently set mean) given a value of its domain.
   */
  public Laplace meanPosterior(double value) {
    return new Laplace(value, b);
  }

  public String toString() {
    return "UnivarLaplace(" + mu + ", " + b + ")";
  }

  private boolean fixedMean;
  private boolean fixedDiversity;

  private double mu;
  private double b;
  private double normConst;
  private double logNormConst;
}
