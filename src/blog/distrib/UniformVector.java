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

import java.util.Iterator;
import java.util.List;

import blog.common.Util;
import blog.common.numerical.MatrixFactory;
import blog.common.numerical.MatrixLib;

/**
 * The uniform distribution over n-dimensional column vectors coming from a
 * specified n-dimensional "box".
 * 
 * @author unknown
 * @author leili
 * @date 2014/05/15
 */

public class UniformVector extends AbstractCondProbDistrib {

  /**
   * The constructor takes an even number of parameters. For all i,
   * 0<=i<=(n-1)/2, the (2i)-th parameter is treated as the lower bound of the
   * generated vectors' i-th component, and every (2i+1)-th argument as the i-th
   * component upper bound. All parameters must be of type Number.
   * 
   * @throws IllegalArgumentException
   */
  public UniformVector(List params) {
    if ((params.size() % 2) != 0) {
      throw new IllegalArgumentException(
          "Uniform vector distribution require an even number "
              + "of parameters, not " + params.size() + ".");
    }
    for (int i = 0; i < params.size(); ++i) {
      if (!(params.get(i) instanceof Number)) {
        throw new IllegalArgumentException("Parameter " + i
            + " to the uniform vector "
            + "distribution must be of class Number, not "
            + params.get(i).getClass() + ".");
      }
    }

    dim = params.size() / 2;
    mins = new double[dim];
    maxes = new double[dim];
    volume = 1;

    int paramIndex = 0;
    for (int i = 0; i < dim; ++i) {
      mins[i] = ((Number) params.get(paramIndex++)).doubleValue();
      maxes[i] = ((Number) params.get(paramIndex++)).doubleValue();
      if (maxes[i] <= mins[i]) {
        throw new IllegalArgumentException("Specified maximum value "
            + maxes[i] + " must be greater than specified minimum " + mins[i]);
      }
      volume *= (maxes[i] - mins[i]);
    }

    densityInBox = 1 / volume;
    logDensityInBox = -Math.log(volume);
  }

  public double getProb(List args, Object value) {
    MatrixLib x = convertValue(value);
    return inBox(x) ? densityInBox : 0;
  }

  public double getLogProb(List args, Object value) {
    MatrixLib x = convertValue(value);
    return inBox(x) ? logDensityInBox : Double.NEGATIVE_INFINITY;
  }

  public Object sampleVal(List args) {
    MatrixLib sample = MatrixFactory.fromArray(new double[dim][1]);
    for (int i = 0; i < dim; ++i) {
      sample.setElement(i, 0, mins[i] + (Util.random() * (maxes[i] - mins[i])));
    }
    return sample;
  }

  private MatrixLib convertValue(Object value) {
    if (value instanceof List) {
      double[][] v = new double[((List) value).size()][1];
      int i = 0;
      for (Iterator it = ((List) value).iterator(); it.hasNext();) {
        List a = (List) it.next();
        v[i][0] = (Double) a.get(0);
        i++;
      }
      value = MatrixFactory.fromArray(v);
    }

    if (!((value instanceof MatrixLib) && (((MatrixLib) value).numCols() == 1))) {
      throw new IllegalArgumentException(
          "The value passed to the uniform vector distribution's "
              + "getProb() method must be a column vector. But it is a "
              + value.getClass());
    }
    MatrixLib x = (MatrixLib) value;

    if (x.numRows() != dim) {
      throw new IllegalArgumentException(
          "The vector passed to the uniform vector distribution's "
              + "getProb method must be " + dim + "-dimensional, not "
              + x.numRows() + "-dimensional");
    }

    return x;
  }

  private boolean inBox(MatrixLib x) {
    for (int i = 0; i < dim; ++i) {
      double val = x.elementAt(i, 0);
      if ((val < mins[i]) || (val > maxes[i])) {
        return false;
      }
    }
    return true;
  }

  private int dim;
  private double[] mins;
  private double[] maxes;

  private double volume;
  private double densityInBox;
  private double logDensityInBox;
}
