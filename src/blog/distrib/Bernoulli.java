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
 * A distribution over {0,1}. It takes one parameter, which is the probability
 * of <code>true</code>.
 */
public class Bernoulli extends AbstractCondProbDistrib {

  public Bernoulli() {
  }

  public Bernoulli(double p) {
    setParams(p);
  }

  public double getP() {
    return p;
  }

  @Override
  public void setParams(Object[] params) {
    if (params.length != 1) {
      throw new IllegalArgumentException(
          "expected one parameter: probability of success");
    }
    setParams((Double) params[0]);
  }

  public void setParams(Double p) {
    if (p != null) {
      if (p < 0 || p > 1) {
        throw new IllegalArgumentException(
            "Parameter to Bernoulli must be in interval [0, 1], not " + p + ".");
      }
      this.p = p;
      this.hasP = true;
    }
  }

  private void checkHasParams() {
    if (!hasP) {
      throw new IllegalArgumentException("p not provided");
    }
  }

  @Override
  public double getProb(Object value) {
    return getProb(((Double) value).doubleValue());
  }

  public double getProb(double value) {
    checkHasParams();
    if (value == 1) {
      return p;
    } else if (value == 0) {
      return 1 - p;
    } else {
      throw new IllegalArgumentException(
          "Bernoulli distribution is over the set {0, 1}; passed value: "
              + value + ".");
    }
  }

  @Override
  public double getLogProb(Object value) {
    return getLogProb(((Double) value).doubleValue());
  }

  public double getLogProb(double value) {
    return Math.log(getProb(value));
  }

  @Override
  public Object sampleVal() {
    checkHasParams();
    if (Util.random() < p) {
      return 1;
    } else {
      return 0;
    }
  }

  public String toString() {
    return "Bernoulli(" + p + ")";
  }

  private double p;
  private boolean hasP;

}
