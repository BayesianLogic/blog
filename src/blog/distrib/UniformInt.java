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
 * Uniform distribution over a range of integers. This distribution has two
 * parameters: <code>lower</code>, which indicates the lower end of the range
 * and <code>upper</code>, which indicates upper end of the range. The
 * range is inclusive (it includes the upper and lower ends).
 */
public class UniformInt implements CondProbDistrib {

  public int getLower() {
    return lower;
  }

  public int getUpper() {
    return upper;
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#setParams(java.util.List)
   */
  /**
   * params[0] -> lower, as defined in UniformInt class description
   * params[1] -> upper, as defined in UniformInt class description
   */
  @Override
  public void setParams(Object[] params) {
    if (params.length != 2) {
      throw new IllegalArgumentException("expected two parameters");
    }
    setParams((Integer) params[0], (Integer) params[1]);
  }

  /**
   * @param lower
   * @param upper
   */
  public void setParams(Integer lower, Integer upper) {
    if (lower != null) {
      this.lower = lower;
      this.hasLower = true;
    }
    if (upper != null) {
      this.upper = upper;
      this.hasUpper = true;
    }
    if (this.hasLower && this.hasUpper) {
      if (lower > upper) {
        throw new IllegalArgumentException(
            "UniformInt distribution requires that lower <= upper");
      }
    }
  }

  private void checkHasParams() {
    if (!this.hasLower) {
      throw new IllegalArgumentException("parameter lower not provided");
    }
    if (!this.hasUpper) {
      throw new IllegalArgumentException("parameter upper not provided");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#getProb(java.lang.Object)
   */
  @Override
  public double getProb(Object value) {
    return getProb(((Integer) value).intValue());
  }

  /**
   * Returns the probability of the UniformInt distribution having outcome
   * <code>value</code>.
   */
  public double getProb(int value) {
    checkHasParams();
    if (value >= lower && value <= upper) {
      return 1.0 / (upper - lower + 1);
    }
    return 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#getLogProb(java.lang.Object)
   */
  @Override
  public double getLogProb(Object value) {
    return getLogProb(((Integer) value).intValue());
  }

  /**
   * Returns the log probability of the UniformInt distribution having outcome
   * <code>value</code>.
   */
  public double getLogProb(int value) {
    return Math.log(getProb(value));
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#sampleVal()
   */
  @Override
  public Object sampleVal() {
    checkHasParams();
    double x = lower + Math.floor(Util.random() * (upper - lower + 1));
    return new Integer((int) x);
  }

  private int lower;
  private boolean hasLower;
  private int upper;
  private boolean hasUpper;
}
