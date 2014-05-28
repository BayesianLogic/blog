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

import java.util.List;

import blog.common.Util;

/**
 * A distribution over Boolean values. It is defined by one parameter, which is
 * the probability of <code>true</code>. This parameter can be specified when
 * the distribution is initialized, in which case the distribution takes no
 * arguments; or the distribution can be initialized with no parameters, in
 * which case it takes the probability as an argument.
 */
public class BooleanDistrib extends AbstractCondProbDistrib {
  /**
   * Creates a new Boolean distribution with the probability of
   * <code>true</code> set to pi.
   */
  public BooleanDistrib(double pi) {
    fixedParam = true;
    setSuccessProb(pi);
  }

  /**
   * Creates a new Boolean distribution. If <code>params</code> is empty, then
   * the distribution takes one CPD argument, which is the probability of
   * <code>true</code>. Otherwise, <code>params</code> should contain one
   * element, a Number specifying this probability.
   */
  public BooleanDistrib(List params) {
    if (params.size() == 0) {
      fixedParam = false;
    } else if (params.size() == 1) {
      fixedParam = true;
      Object paramObj = params.get(0);
      if (!(paramObj instanceof Number)) {
        throw new IllegalArgumentException(
            "Parameter to Bernoulli distrib must be of class " + "Number, not "
                + paramObj.getClass() + ".");
      }

      pi = ((Number) paramObj).doubleValue();
      setSuccessProb(pi);
    } else {
      throw new IllegalArgumentException(
          "Bernoulli distribution takes at most one parameter.");
    }
  }

  /**
   * Returns the probability of the given Boolean value under this distribution.
   * This method should only be called if this distribution was initialized with
   * a fixed success probability, or if <code>ensureProbsInited</code> has just
   * been called.
   */
  public double getProb(boolean value) {
    if (value) {
      return pi;
    }

    return (1 - pi);
  }

  public double getProb(List args, Object value) {
    ensureParamsInited(args);

    if (!(value instanceof java.lang.Boolean)) {
      throw new IllegalArgumentException(
          "Bernoulli distribution is over objects of class Boolean, " + "not "
              + value.getClass() + ".");
    }

    return getProb(((java.lang.Boolean) value).booleanValue());
  }

  /**
   * Returns the log probability of the given Boolean value under this
   * distribution. This method should only be called if this distribution was
   * initialized with a fixed success probability, or if
   * <code>ensureProbsInited</code> has just been called.
   */
  public double getLogProb(boolean value) {
    if (value) {
      return Math.log(pi);
    }

    return Math.log(1 - pi);
  }

  /**
   * Returns a boolean value sampled from this distribution. This method should
   * only be called if this distribution was initialized with a fixed success
   * probability, or if <code>ensureProbsInited</code> has just been called.
   */
  public boolean sampleVal() {
    if (Util.random() < pi) {
      return true;
    }
    return false;
  }

  public Object sampleVal(List args) {
    ensureParamsInited(args);
    return java.lang.Boolean.valueOf(sampleVal());
  }

  private void ensureParamsInited(List args) {
    if (fixedParam) {
      if (!args.isEmpty()) {
        throw new IllegalArgumentException(
            "Bernoulli distribution with fixed success probability "
                + "expects no arguments.");
      }
    } else {
      if (args.size() != 1) {
        throw new IllegalArgumentException(
            "Bernoulli distribution without fixed success "
                + "probability expects exactly one argument, specifying "
                + "that probability.");
      }
      Object paramObj = args.get(0);
      if (!(paramObj instanceof Number)) {
        throw new IllegalArgumentException(
            "Parameter to Bernoulli distrib must be of class " + "Number, not "
                + paramObj.getClass() + ".");
      }
      setSuccessProb(((Number) paramObj).doubleValue());
    }
  }

  private void setSuccessProb(double pi) {
    if ((pi < 0) || (pi > 1)) {
      throw new IllegalArgumentException(
          "Parameter to Bernoulli must be in interval [0, 1], not " + pi + ".");
    }

    this.pi = pi;
  }

  private boolean fixedParam;
  private double pi;
}
