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

import blog.common.Util;
import blog.model.Type;

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

  /**
   * Creates a Laplace distribution with the given fixed mean and
   * diversity.
   */
  public Laplace(double mean, double diversity) {
    expectMeanAsArg = false;
    setMean(mean);

    expectDiversityAsArg = false;
    setDiversity(diversity);
  }

  /**
   * Creates a Laplace distribution. If two parameters are given,
   * then the first is the mean and the second is the diversity. If only one
   * parameter is given, it is interpreted as the diversity. Parameters not
   * specified here must be given as arguments to <code>getProb</code> and
   * <code>sampleVal</code>.
   */
  public Laplace(List params) {
    if (params.size() == 0) {
      expectMeanAsArg = true;
      expectDiversityAsArg = true;
    } else if (params.size() == 1) {
      expectMeanAsArg = false;
      expectDiversityAsArg = true;
      initParams(params);
      expectMeanAsArg = true;
      expectDiversityAsArg = false;
    } else if (params.size() == 2) {
      expectMeanAsArg = true;
      expectDiversityAsArg = true;
      initParams(params);
      expectMeanAsArg = false;
      expectDiversityAsArg = false;
    } else {
      throw new IllegalArgumentException(
          "Laplace CPD expects at most 2 parameters, not " + params.size());
    }
  }

  public double getProb(List args, Object value) {
    initParams(args);
    if (!(value instanceof Number))
      throw new IllegalArgumentException(
          "The value passed to the Laplace distribution's " + "getProb "
              + "method must be of type Number, not " + value.getClass() + ".");
    return getProb(((Number) value).doubleValue());
  }

  public double getLogProb(List args, Object value) {
    initParams(args);
    if (!(value instanceof Number))
      throw new IllegalArgumentException(
          "The value passed to the Laplace distribution's " + "getLogProb "
              + "method must be of type Number, not " + value.getClass() + ".");
    return getLogProb(((Number) value).doubleValue());
  }

  /**
   * Returns the density of this Laplace distribution at the given value. This
   * method should only be called if the mean and diversity were set in the
   * constructor (internal calls are ok if the private method
   * <code>initParams</code> is called first).
   */
  public double getProb(double x) {
    return (Math.exp(-Math.abs(x - mean) / diversity) / normConst);
  }

  /**
   * Returns the natural log of the density of this Laplace distribution at the
   * given value. This method should only be called if the mean and diversity
   * were set in the constructor, or if <code>initParams</code> has been called.
   */
  public double getLogProb(double x) {
    return (-Math.abs(x - mean) / diversity) - logNormConst;
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
      return mean - diversity * Math.log(1 - 2 * U);
    else
      return mean + diversity * Math.log(1 + 2 * U);
  }

  private void initParams(List args) {
    if (args.size() > 2) {
      throw new IllegalArgumentException(
          "Laplace CPD expects at most 2 parameters, not " + args.size());
    }
    if (!expectMeanAsArg) {
      if (!expectDiversityAsArg) {
        if (args.size() > 0) {
          throw new IllegalArgumentException(
              "Laplace CPD with fixed mean expects no " + "arguments.");
        }
      } else {
        if (!(args.get(0) instanceof Number)) {
          throw new IllegalArgumentException(
              "The divesity parameter of Laplace CPD must be of type Number, not "
                  + args.get(0).getClass() + ".");
        } else {
          Double Diversity = (Double) args.get(0);
          if (Diversity <= 0) {
            throw new IllegalArgumentException(
                "Laplace CPD expects a positive real number as the diversity parameter, not "
                    + Diversity + ".");
          } else {
            setDiversity(Diversity);
          }
        }
      }
    } else {
      if (args.size() < 1) {
        throw new IllegalArgumentException(
            "Laplace CPD created without a fixed mean; "
                + "requires mean as an argument.");
      }
      if (!(args.get(0) instanceof Number)) {
        throw new IllegalArgumentException(
            "The mean parameter of Laplace CPD must be of type Number, not "
                + args.get(0).getClass() + ".");
      } else {
        setMean((Double) args.get(0));
      }
      if (!expectDiversityAsArg) {
        if (args.size() > 1) {
          throw new IllegalArgumentException(
              "Laplace CPD with fixed Laplace expects " + "only one argument.");
        }
      } else {
        if (args.size() < 2) {
          throw new IllegalArgumentException(
              "Laplace CPD created without a fixed "
                  + "Laplace; requires diversity as argument.");
        }
        if (!(args.get(1) instanceof Number)) {
          throw new IllegalArgumentException(
              "The divesity parameter of Laplace CPD must be of type Number, not "
                  + args.get(1).getClass() + ".");
        } else {
          double Diversity = (Double) args.get(1);
          if (Diversity <= 0) {
            throw new IllegalArgumentException(
                "Laplace CPD expects a positive real number as the diversity parameter, not "
                    + Diversity + ".");
          } else {
            setDiversity(Diversity);
          }
        }
      }
    }
  }

  private void setMean(double mean) {
    this.mean = mean;
  }

  public double getMean() {
    return mean;
  }

  public double getDiversity() {
    return diversity;
  }

  private void setDiversity(double diversity) {
    if (diversity <= 0) {
      throw new IllegalArgumentException(
          "Diversity of Laplace distribution must be positive, " + "not "
              + diversity);
    }
    this.diversity = diversity;
    normConst = diversity * 2;
    logNormConst = Math.log(diversity) + Math.log(2);
  }

  public String toString() {
    return "Laplace(" + mean + ", " + diversity + ")";
  }

  private boolean expectMeanAsArg;
  private boolean expectDiversityAsArg;

  private double mean;
  private double diversity;
  private double normConst;
  private double logNormConst;
}
