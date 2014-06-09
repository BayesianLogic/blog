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

import blog.common.Util;

/**
 * A distribution over Boolean values. It is defined by one parameter p, which
 * is the probability of <code>true</code>.
 */
public class BooleanDistrib implements CondProbDistrib {

  public double getP() {
    return p;
  }

  /**
   * params[0] -> p, probability of <code>true</code>
   */
  @Override
  public void setParams(Object[] params) {
    if (params.length != 1) {
      throw new IllegalArgumentException(
          "expected one parameter: probability of success");
    }
    setParams((Double) params[0]);
  }

  /**
   * @param p
   *          probability of <true>
   */
  public void setParams(Double p) {
    if (p != null) {
      if (p > 1 || p < 0) {
        throw new IllegalArgumentException(
            "Parameter to Bernoulli must be in interval [0, 1], not " + p + ".");
      }
      this.p = p;
      this.logP = Math.log(p);
      this.log1_P = Math.log(1 - p);
      this.hasP = true;
    }
  }

  private void checkHasParams() {
    if (!this.hasP) {
      throw new IllegalArgumentException("parameter p not provided");
    }
  }

  @Override
  public double getProb(Object value) {
    return getProb(((Boolean) value).booleanValue());
  }

  /**
   * Returns the probability of the given Boolean value under this distribution.
   **/
  public double getProb(boolean value) {
    checkHasParams();
    if (value) {
      return p;
    }
    return 1 - p;
  }

  @Override
  public double getLogProb(Object value) {
    return getLogProb(((Boolean) value).booleanValue());
  }

  /**
   * Returns the log probability of the given Boolean value under this
   * distribution.
   */
  public double getLogProb(boolean value) {
    checkHasParams();
    if (value) {
      return logP;
    }
    return log1_P;
  }

  @Override
  public Object sampleVal() {
    if (Util.random() < p) {
      return true;
    }
    return false;
  }

  public String toString() {
    return "BooleanDistrib(" + p + ")";
  }

  private double p;
  private double logP; // log p
  private double log1_P; // log (1 - p)
  private boolean hasP;

}
