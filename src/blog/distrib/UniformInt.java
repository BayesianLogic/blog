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
 * 
 * @since June 11, 2014
 */
public class UniformInt implements CondProbDistrib {

  /**
   * Returns the parameter <code>lower</code>.
   */
  public int getLower() {
    return lower;
  }

  /**
   * Returns the parameter <code>upper</code>.
   */
  public int getUpper() {
    return upper;
  }

  /**
   * Mapping for the parameter <code>params</code>
   * <ul>
   * <li>params[0]: <code>lower</code></li>
   * <li>params[1]: <code>upper</code></li>
   * </ul>
   * 
   * @see blog.distrib.CondProbDistrib#setParams(java.util.List)
   */
  @Override
  public void setParams(Object[] params) {
    if (params.length != 2) {
      throw new IllegalArgumentException("expected two parameters");
    }
    setParams((Integer) params[0], (Integer) params[1]);
  }

  /**
   * For a non-null value of method parameter lower, sets the
   * distribution parameter <code>lower</code> to method parameter lower.
   * Similarly for <code>upper</code>. Then checks to see if assignment of
   * parameters is legal. In other words, an assignment of parameters is legal
   * if <code>lower <= upper</code>.
   * 
   * @param lower
   *          parameter <code>lower</code>
   * @param upper
   *          parameter <code>upper</code>
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
      if (this.lower > this.upper) {
        throw new IllegalArgumentException(
            "UniformInt distribution requires that lower <= upper");
      }
      this.prob = 1.0 / (this.upper - this.lower + 1);
      this.logProb = Math.log(this.prob);
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
    return (value >= lower) && (value <= upper) ? prob : 0;
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
    checkHasParams();
    return (value >= lower) && (value <= upper) ? logProb
        : Double.NEGATIVE_INFINITY;
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#sampleVal()
   */
  @Override
  public Object sampleVal() {
    checkHasParams();
    return lower + Util.randInt(upper - lower + 1);
  }

  /** Parameter <code>lower</code>. */
  private int lower;
  /** Flag indicating whether <code>lower</code> has been set. */
  private boolean hasLower;
  /** Parameter <code>upper</code>. */
  private int upper;
  /** Flag indicating whether <code>upper</code> has been set. */
  private boolean hasUpper;
  /**
   * The probability of an outcome between <code>lower</code> and
   * <code>upper</code> inclusive.
   */
  private double prob;
  /**
   * The log probability of an outcome between <code>lower</code> and
   * <code>upper</code> inclusive.
   */
  private double logProb;
}
