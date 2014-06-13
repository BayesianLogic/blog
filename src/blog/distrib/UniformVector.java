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
import blog.common.numerical.MatrixFactory;
import blog.common.numerical.MatrixLib;

/**
 * The uniform distribution over n-dimensional column vectors coming from a
 * specified n-dimensional "box". <code>box</code> is an n by 2 MatrixLib
 * instance where each row represents the dimension, and the first column of
 * each row represents the lower bound of that dimension and the second column
 * of that row represents the corresponding upper bound of that dimension.
 * 
 * @author leili
 * @date May 15, 2014
 * @since June 14, 2014
 */

public class UniformVector implements CondProbDistrib {

  private boolean inBox(MatrixLib x) {
    for (int i = 0; i < dim; i++) {
      double val = x.elementAt(i, 0);
      if ((val < mins[i]) || (val > maxes[i])) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns the lower bounds for each dimension.
   */
  public double[] getLower() {
    return mins;
  }

  /**
   * Returns the upper bounds for each dimension.
   */
  public double[] getUpper() {
    return maxes;
  }

  /**
   * mapping for <code>params</code>:
   * 
   * <ul>
   * <li>params[0]: box, an N by 2 MatrixLib instance</li>
   * </ul>
   * 
   * @see blog.distrib.CondProbDistrib#setParams(java.util.List)
   */
  @Override
  public void setParams(Object[] params) {
    if (params.length != 1) {
      throw new IllegalArgumentException("expected one parameter");
    }
    setParams((MatrixLib) params[0]);
  }

  /**
   * 
   * @param box
   *          An <code>N</code> by 2 dimensional MatrixLib. Each row represents
   *          a dimension, where the first column represents the lower bound of
   *          that dimension and the second column represents the upper bound of
   *          that dimension. This parameter is valid if for all dimensions, the
   *          lower bound is strictly less than the upper bound.
   */
  public void setParams(MatrixLib box) {
    if (box != null) {
      if (box.numCols() != 2 || box.numRows() == 0) {
        throw new IllegalArgumentException(
            "Incorrect dimensions given for UniformVector. Expecting an N by 2 MatrixLib where N >= 1");
      }
      volume = 1.0;
      mins = new double[box.numRows()];
      maxes = new double[box.numRows()];
      for (int i = 0; i < box.numRows(); i++) {
        mins[i] = box.elementAt(i, 0);
        maxes[i] = box.elementAt(i, 1);
        if (mins[i] >= maxes[i]) {
          this.hasBox = false;
          throw new IllegalArgumentException(
              "All dimensions must have a min that is lower than the corresponding max. For dimension "
                  + (i + 1) + " min = " + mins[i] + " and max = " + maxes[i]);
        }
        volume *= (maxes[i] - mins[i]);
      }
      this.dim = mins.length;
      this.densityInBox = 1.0 / volume;
      this.logDensityInBox = -Math.log(volume);
      this.hasBox = true;
    }
  }

  private void checkHasParams() {
    if (!this.hasBox) {
      throw new IllegalArgumentException(
          "UniformVector not properly initialized");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#getProb(java.lang.Object)
   */
  @Override
  public double getProb(Object value) {
    return getProb((MatrixLib) value);
  }

  /**
   * Return the probability of <code>value</code>.
   * 
   * @param value
   * @return
   */
  public double getProb(MatrixLib value) {
    checkHasParams();
    if (value.numRows() != dim || value.numCols() != 1) {
      throw new IllegalArgumentException("The outcome matrix should be " + dim
          + " by 1. Instead got a " + value.numRows() + " by "
          + value.numCols() + " matrix.");
    }
    return inBox(value) ? this.densityInBox : 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#getLogProb(java.lang.Object)
   */
  @Override
  public double getLogProb(Object value) {
    return getLogProb((MatrixLib) value);
  }

  /**
   * Return the log probability of <code>value</code>.
   * 
   * @param value
   * @return
   */
  public double getLogProb(MatrixLib value) {
    checkHasParams();
    if (value.numRows() != dim || value.numCols() != 1) {
      throw new IllegalArgumentException("The outcome matrix should be " + dim
          + " by 1. Instead got a " + value.numRows() + " by "
          + value.numCols() + " matrix.");
    }
    return inBox(value) ? this.logDensityInBox : Double.NEGATIVE_INFINITY;
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#sampleVal()
   */
  @Override
  public Object sampleVal() {
    checkHasParams();
    MatrixLib sample = MatrixFactory.fromArray(new double[dim][1]);
    for (int i = 0; i < dim; ++i) {
      sample.setElement(i, 0, mins[i] + (Util.random() * (maxes[i] - mins[i])));
    }
    return sample;
  }

  @Override
  public String toString() {
    return getClass().getName();
  }

  private int dim;
  private double[] mins;
  private double[] maxes;
  private boolean hasBox;

  private double volume;
  private double densityInBox;
  private double logDensityInBox;

}
