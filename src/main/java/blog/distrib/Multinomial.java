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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import blog.common.Util;
import blog.common.numerical.MatrixFactory;
import blog.common.numerical.MatrixLib;

/**
 * The multinomial distribution accepts two parameters, an integer
 * <code>N</code> representing the number of trials, and <code>P</code>, an
 * unnormalized column vector representing the unnormalized probabilities for
 * each of the categories.
 * 
 * See https://en.wikipedia.org/wiki/Multinomial_distribution
 * 
 * @author cgioia
 * @since June 17, 2014
 */
public class Multinomial implements CondProbDistrib {

  /**
   * set parameters for multinomial distribution.
   * 
   * @param params
   *          an array of the form [Integer, MatrixLib]
   *          <ul>
   *          <li>params[0]: <code>N</code>, number of trials (Integer)</li>
   *          <li>params[1]: <code>P</code>, probability of success for each
   *          category (MatrixLib, column vector)</li>
   *          </ul>
   * 
   * @see blog.distrib.CondProbDistrib#setParams(java.util.List)
   */
  @Override
  public void setParams(Object[] params) {
    if (params.length != 2) {
      throw new IllegalArgumentException("expected two parameters");
    }
    setParams((Integer) params[0], (MatrixLib) params[1]);
  }

  /**
   * If method parameter n is non-null, then attempt to set distribution
   * parameter <code>N</code> to n if n is a nonnegative integer.
   * If method parameter p is non-null, then set distribution parameter
   * <code>P</code> to method parameter p.
   * 
   * @param n
   *          distribution parameter <code>N</code>, representing the number of
   *          categories. Must be nonnegative. Can be set multiple times.
   * 
   * @param p
   *          distribution parameter <code>P</code>, representing the column
   *          vector of unnormalized probabilities.
   */
  public void setParams(Integer n, MatrixLib p) {
    if (n != null) {
      if (n < 0) {
        throw new IllegalArgumentException(
            "The number of trials 'n' for a Multinomial Distribution must be nonnegative");
      }
      this.n = n;
      this.hasN = true;
    }
    if (p != null) {
      if (p.numCols() != 1 || p.numRows() == 0) {
        throw new IllegalArgumentException(
            "The vector p passed in is not a column vector");
      }
      initializeProbabilityVector(p);
      this.hasP = true;
    }
  }

  /**
   * Precondition: p is a column vector
   * 
   * Sets instance variable p to a normalized array of probabilities
   * Sets pCDF to the CDF of p
   * 
   * @param p
   */
  private void initializeProbabilityVector(MatrixLib p) {
    double sum = 0.0;
    for (int i = 0; i < p.numRows(); i++) {
      double ele = p.elementAt(i, 0);
      if (ele < 0) {
        throw new IllegalArgumentException("Probability " + ele
            + " for element " + i + " is negative.");
      }
      sum += ele;
    }
    if (sum < 1e-9) {
      throw new IllegalArgumentException("Probabilities sum to approx zero");
    }
    this.k = p.numRows();
    this.p = new double[k];
    this.pCDF = new double[k];
    this.p[0] = p.elementAt(0, 0) / sum;
    pCDF[0] = this.p[0];
    for (int i = 1; i < p.numRows(); i++) {
      this.p[i] = p.elementAt(i, 0) / sum;
      this.pCDF[i] = pCDF[i - 1] + this.p[i];
    }
  }

  private void checkHasParams() {
    if (!this.hasN) {
      throw new IllegalArgumentException("parameter N not provided");
    }
    if (!this.hasP) {
      throw new IllegalArgumentException("parameter P not provided");
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
   * Returns the probability of value outcomes, where value is a row vector
   * representing the number of times each category occurred.
   * 
   * @param value
   */
  public double getProb(MatrixLib value) {
    checkHasParams();
    if (!inSupport(value)) {
      return 0.0;
    }
    double prob = Util.factorial(n);
    for (int i = 0; i < k; i++) {
      prob *= Math.pow(p[i], value.elementAt(i, 0));
      prob /= Util.factorial((int) Math.round(value.elementAt(i, 0)));
    }
    return prob;
  }

  /**
   * Returns whether or not the row vector <code>value</code> is a possible
   * combination of occurrences for this multinomial distribution. In other
   * words, returns true iff <code>value</code> is in the support of this
   * multinomial distribution. Refer to
   * http://en.wikipedia.org/wiki/Multinomial_distribution.
   * 
   * @throws IllegalArgumentException
   *           if value is not a row vector of the correct dimension (1 by k)
   */
  private boolean inSupport(MatrixLib value) {
    if (value.numCols() != 1 || value.numRows() != k) {
      throw new IllegalArgumentException(
          "The matrix provided is of the incorrect dimensions. Expecting a "
              + this.k
              + " by 1 column vector but instead got a matrix of dimension "
              + value.numRows() + " by " + value.numCols());
    }
    double sum = 0.0;
    for (int i = 0; i < k; i++) {
      double element = value.elementAt(i, 0);
      if (element < 0 || element % 1 != 0.0) {
        // Number of successes for a particular category is negative or not an
        // integer.
        return false;
      }
      sum += element;
    }
    if (sum != n) {
      return false; // N != the sum of values
    }
    return true;
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
   * Returns the log probability of value outcomes, where value is a row vector
   * representing the number of times each category occurred.
   * 
   * @param value
   */
  public double getLogProb(MatrixLib value) {
    checkHasParams();
    if (!inSupport(value)) {
      return Double.NEGATIVE_INFINITY;
    }
    double logProb = Util.logFactorial(n);
    for (int i = 0; i < k; i++) {
      logProb += value.elementAt(i, 0) * Math.log(p[i]);
      logProb -= Util.logFactorial((int) Math.round(value.elementAt(i, 0)));
    }
    return logProb;
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

  /** Samples a value from the multinomial. */
  public MatrixLib sample_value() {
    checkHasParams();
    // result actually stores integers, but we declare it as double because we
    // don't have support for int matrices
    double[] result = new double[k];
    for (int i = 0; i < k; i++) {
      result[i] = 0;
    }

    for (int trial = 0; trial < n; trial++) {
      double val = Util.random();
      int bucket;
      for (bucket = 0; bucket < k; bucket++) {
        if (val <= pCDF[bucket]) {
          break;
        }
      }
      result[bucket] += 1;
    }
    return MatrixFactory.createColumnVector(result);
  }

  @Override
  public List<MatrixLib> getFiniteSupport() {
    checkHasParams();
    List<MatrixLib> finiteSupport = new ArrayList<MatrixLib>();
    MatrixLib mat = MatrixFactory.zeros(n, 1);
    calculateFiniteSupport(finiteSupport, mat, 0, n);
    return Collections.unmodifiableList(finiteSupport);
  }

  private void calculateFiniteSupport(List<MatrixLib> finiteSupport,
      MatrixLib mat, int depth, int remain) {
    if (depth == n)
      finiteSupport.add(mat);
    else if (depth == n - 1) {
      if (remain == 0)
        calculateFiniteSupport(finiteSupport, mat, depth + 1, 0);
      else if (!Util.closeToZero(p[depth])) {
        mat.setElement(depth, 0, remain);
        calculateFiniteSupport(finiteSupport, mat, depth + 1, 0);
      }
    } else {
      calculateFiniteSupport(finiteSupport, mat, depth + 1, remain);
      if (!Util.closeToZero(p[depth])) {
        for (int i = 1; i <= remain; i++) {
          mat.setElement(depth, 0, i);
          calculateFiniteSupport(finiteSupport, mat, depth + 1, remain - i);
        }
      }
    }
  }

  private int n; // the number of trials
  private boolean hasN;
  private double[] p; // probability vector
  private double[] pCDF;
  private boolean hasP;
  private int k; // the number of categories; dimension of p
}
