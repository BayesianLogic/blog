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

/**
 * Uniform distribution over a range of real numbers [lower, upper). The range
 * is open at the upper end for consistency with Random.nextDouble().
 */
public class UniformReal implements CondProbDistrib {
  public UniformReal() {
  }

  public UniformReal(double lower, double upper) {
    setParams(lower, upper);
  }

  public void setParams(Double lower, Double upper) {
    if (lower != null) {
      this.lower = lower;
      this.hasLower = true;
    }
    if (upper != null) {
      this.upper = upper;
      this.hasUpper = true;
    }
    if (this.hasLower && this.hasUpper && this.lower >= this.upper) {
      throw new IllegalArgumentException("lower >= upper");
    }
  }

  public void setParams(List<Object> params) {
    if (params.size() != 2) {
      throw new IllegalArgumentException("expected two params: lower and upper");
    }
    setParams((Double) params.get(0), (Double) params.get(1));
  }

  private void checkHasParams() {
    if (!hasLower) {
      throw new IllegalArgumentException("lower not provided");
    }
    if (!hasUpper) {
      throw new IllegalArgumentException("upper not provided");
    }
  }

  public double getProb(Object value) {
    return getProb(((Double) value).doubleValue());
  }

  public double getProb(double value) {
    checkHasParams();
    if ((value >= lower) && (value < upper)) {
      return 1.0 / (upper - lower);
    } else {
      return 0.0;
    }
  }

  public double getLogProb(Object value) {
    return getLogProb(((Double) value).doubleValue());
  }

  public double getLogProb(double value) {
    return Math.log(getProb(value));
  }

  public Object sampleVal() {
    checkHasParams();
    // rely on the fact that Util.random() returns a value in [0, 1)
    double x = lower + (Util.random() * (upper - lower));
    return x;
  }

  public double getLower() {
    return lower;
  }

  public double getUpper() {
    return upper;
  }

  public String toString() {
    return "U[" + lower + ", " + upper + "]";
  }

  private boolean hasLower;
  private boolean hasUpper;

  private double lower;
  private double upper;
}
