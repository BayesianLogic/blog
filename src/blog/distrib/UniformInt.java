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
 * Uniform distribution over a range of integers. This distribution has two
 * parameters: the lower end of the range and the upper end of the range. The
 * range is inclusive (it includes the upper and lower ends).
 */
public class UniformInt extends AbstractCondProbDistrib {
  /**
   * Interprets the parameters as a pair of integers (lower, upper) and creates
   * a uniform distribution over the range {lower, ..., upper}.
   * 
   * @throws IllegalArgumentException
   *           if params does not consist of exactly two Integer objects, or if
   *           lower > upper
   */
  public UniformInt(List params) {
    try {
      lower = ((Number) params.get(0)).intValue();
      upper = ((Number) params.get(1)).intValue();
      if ((lower > upper) || (params.size() > 2)) {
        throw new IllegalArgumentException();
      }
    } catch (RuntimeException e) {
      throw new IllegalArgumentException(
          "UniformInt CPD expects two integer arguments "
              + "[lower, upper] with lower <= upper.  Got: " + params);
    }
  }

  /**
   * Returns 1 / (upper - lower + 1) if the given integer is in the range of
   * this distribution, otherwise returns zero. Takes no arguments.
   * 
   * @throws IllegalArgumentException
   *           if <code>args</code> is non-empty or <code>value</code> is not an
   *           Integer
   */
  public double getProb(List args, Object value) {
    if (!args.isEmpty()) {
      throw new IllegalArgumentException(
          "UniformInt CPD does not take any arguments.");
    }
    if (!(value instanceof Integer)) {
      throw new IllegalArgumentException(
          "UniformInt CPD defines distribution over objects of class "
              + "Integer, not " + value.getClass() + ".");
    }
    int x = ((Integer) value).intValue();

    if ((x >= lower) && (x <= upper)) {
      return 1.0 / (upper - lower + 1);
    }
    return 0;
  }

  /**
   * Returns a sample from this distribution.
   * 
   * @throws IllegalArgumentException
   *           if <code>args</code> is non-empty
   */
  public Object sampleVal(List args) {
    if (!args.isEmpty()) {
      throw new IllegalArgumentException(
          "UniformInt CPD does not take any arguments.");
    }

    // rely on the fact that Util.random() returns a value in [0, 1)
    double x = lower + Math.floor(Util.random() * (upper - lower + 1));
    return new Integer((int) x);
  }

  private int lower;
  private int upper;

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#setParams(java.util.List)
   */
  @Override
  public void setParams(Object[] params) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#getProb(java.lang.Object)
   */
  @Override
  public double getProb(Object value) {
    // TODO Auto-generated method stub
    return 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#getLogProb(java.lang.Object)
   */
  @Override
  public double getLogProb(Object value) {
    // TODO Auto-generated method stub
    return 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#sampleVal()
   */
  @Override
  public Object sampleVal() {
    // TODO Auto-generated method stub
    return null;
  }
}
