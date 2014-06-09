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
 * An Exponential distribution with parameter lambda over non-negative reals.
 * The probability of x is lambda * e^(-lambda*x).
 */

public class Exponential implements CondProbDistrib {

  public double getLambda() {
    return lambda;
  }

  @Override
  /**
   * params[0] -> lambda, as defined in the class description
   */
  public void setParams(Object[] params) {
    if (params.length != 1) {
      throw new IllegalArgumentException("expected one parameter");
    }
    setParams((Double) params[0]);
  }

  public void setParams(Double lambda) {
    if (lambda != null) {
      if (lambda <= 0) {
        throw new IllegalArgumentException(
            "parameter lambda for an exponential distribution must be a strictly positive real");
      }
      this.hasLambda = true;
      this.lambda = lambda;
    }
  }

  public void checkHasParams() {
    if (!this.hasLambda) {
      throw new IllegalArgumentException("parameter lambda not provided");
    }
  }

  @Override
  public double getProb(Object value) {
    return getProb(((Double) value).doubleValue());
  }

  public double getProb(double value) {
    checkHasParams();
    if (value < 0) {
      return 0;
    } else {
      return (lambda * Math.exp((-lambda) * value));
    }
  }

  @Override
  public double getLogProb(Object value) {
    return getLogProb(((Double) value).doubleValue());
  }

  public double getLogProb(double value) {
    checkHasParams();
    if (value < 0) {
      return Double.NEGATIVE_INFINITY;
    } else {
      return Math.log(lambda) - lambda * value;
    }
  }

  @Override
  public Object sampleVal() {
    checkHasParams();
    return Exponential.sampleVal(lambda);
  }

  /**
   * Generate a value from Exponential distribution with parameter lambda.
   */
  public static double sampleVal(double lambda) {
    return -Math.log(Util.random()) / lambda;
  }

  private boolean hasLambda;
  private double lambda;
}
