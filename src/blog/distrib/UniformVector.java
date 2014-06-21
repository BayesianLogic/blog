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
 * specified n-dimensional "box".
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
   * set parameters for UniformVector distribution
   * 
   * @param params
   *          An array of MatrixLib instances in the form of 1 by 2 row vectors.
   *          The first element of the row vector represents the lower bound of
   *          the dimension and the second of the row vector represents the
   *          corresponding upper bound of the dimension.
   * 
   * @see blog.distrib.CondProbDistrib#setParams(java.util.List)
   */
  @Override
  public void setParams(Object[] params) {
    if (this.hasDimension) {
      if (params.length != dim) {
        throw new IllegalArgumentException("expected " + dim
            + " arguments to setParams for UniformVector distribution. Got "
            + params.length + " dimensions");
      }
    } else {
      this.numFilled = 0;
      this.dim = params.length;
      this.hasDimension = true;
      this.mins = new double[dim];
      this.maxes = new double[dim];
      this.dimensionSet = new boolean[dim];
    }
    MatrixLib[] m = new MatrixLib[dim];
    System.arraycopy(params, 0, m, 0, params.length);
    setParams(m);
  }

  /**
   * @param vectors
   *          An array of MatrixLib instances of length <code>dim</code> in the
   *          form of 1 by 2 row vectors.
   */
  public void setParams(MatrixLib[] vectors) {
    for (int i = 0; i < vectors.length; i++) {
      MatrixLib rowVector = vectors[i];
      if (rowVector == null) {
        continue;
      }
      if (rowVector.numCols() != 2 || rowVector.numRows() != 1) {
        this.hasBox = false;
        throw new IllegalArgumentException(
            "expecting a row vector of dimension 1 by 2; instead got a matrix of dimension "
                + rowVector.numRows() + " by " + rowVector.numCols());
      }
      if (this.dimensionSet[i]) {
        this.volume /= (this.maxes[i] - this.mins[i]);
        this.mins[i] = rowVector.elementAt(0, 0);
        this.maxes[i] = rowVector.elementAt(0, 1);
        this.volume *= (this.maxes[i] - this.mins[i]);
      } else {
        this.mins[i] = rowVector.elementAt(0, 0);
        this.maxes[i] = rowVector.elementAt(0, 1);
        this.volume *= (this.maxes[i] - this.mins[i]);
        this.numFilled += 1;
        this.dimensionSet[i] = true;
      }
    }
    if (this.numFilled == this.dim) {
      this.hasBox = true;
      this.densityInBox = 1.0 / volume;
      this.logDensityInBox = -Math.log(volume);
      StringBuffer s = new StringBuffer();
      s.append("UniformVector(");
      for (int i = 0; i < dim; i++) {
        s.append("[" + this.mins[i] + "," + this.maxes[i] + "] ");
      }
      s.append(")");
      this.stringRepr = s.toString();
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
   *          a <code>dim</code> by 1 MatrixLib column vector
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
   *          a <code>dim</code> by 1 MatrixLib column vector
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
    return sample_value();
  }

  /** Samples from a UniformVector distribution. */
  public MatrixLib sample_value() {
    checkHasParams();
    MatrixLib sample = MatrixFactory.fromArray(new double[dim][1]);
    for (int i = 0; i < dim; ++i) {
      sample.setElement(i, 0, mins[i] + (Util.random() * (maxes[i] - mins[i])));
    }
    return sample;
  }

  @Override
  public String toString() {
    return stringRepr;
  }

  private int dim;
  private int numFilled;
  private boolean hasDimension;
  private boolean[] dimensionSet;
  private double[] mins;
  private double[] maxes;
  private boolean hasBox;

  private double volume = 1.0;
  private double densityInBox;
  private double logDensityInBox;
  private String stringRepr;
}
