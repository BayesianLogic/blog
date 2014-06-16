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
import java.util.List;

import blog.common.Util;
import blog.common.numerical.MatrixFactory;
import blog.common.numerical.MatrixLib;

/**
 * The multinomial distribution accepts two parameters, an integer
 * <code>N</code> representing the number of trials, and <code>P</code>, an
 * unnormalized row vector representing the unnormalized probabilities for each
 * of the categories.
 * 
 * See https://en.wikipedia.org/wiki/Multinomial_distribution
 * 
 * @author cgioia
 * @since June 16, 2014
 */
public class Multinomial implements CondProbDistrib {

  /** Returns the distribution parameter <code>N</code>. */
  public int getN() {
    return n;
  }

  /** Returns the distribution parameter <code>P</code>. */
  public double[] getP() {
    return p;
  }

  MatrixLib ensureValueFormat(Object value) {
    if (!(value instanceof MatrixLib)) {
      throw new IllegalArgumentException("expected vector value");
    }
    final int numBuckets = pi.length;
    MatrixLib valueVector = (MatrixLib) value;
    if (valueVector.numRows() == 1 && (valueVector.numCols() != 1)) {
      valueVector = valueVector.transpose();
    }
    if (valueVector.numRows() != numBuckets) {
      throw new IllegalArgumentException("value has wrong dimension");
    }
    return valueVector;
  }

  /**
   * Returns the probability of given vector.
   */
  public double getProb(List args, Object value) {
    initParams(args);
    final int numBuckets = pi.length;
    MatrixLib valueVector = ensureValueFormat(value);
    int sum = (int) valueVector.columnSum().elementAt(0, 0);
    if (sum != numTrials) {
      return 0;
    }
    double prob = Util.factorial(numTrials);
    for (int i = 0; i < numBuckets; i++) {
      prob *= Math.pow(pi[i], valueVector.elementAt(i, 0));
      prob /= Util.factorial((int) Math.round(valueVector.elementAt(i, 0)));
      // FIXME: It would be better if we could take the param as an array
      // of ints, so we don't have to worry about rounding.
    }
    return prob;
  }

  /**
   * Returns the log probability of given vector.
   */
  public double getLogProb(List args, Object value) {
    initParams(args);
    final int numBuckets = pi.length;
    MatrixLib valueVector = ensureValueFormat(value);
    int sum = (int) valueVector.columnSum().elementAt(0, 0);
    if (sum != numTrials) {
      return 0;
    }
    double logProb = Util.logFactorial(numTrials);
    for (int i = 0; i < numBuckets; i++) {
      logProb += valueVector.elementAt(i, 0) * Math.log(pi[i]);
      logProb -= Util
          .logFactorial((int) Math.round(valueVector.elementAt(i, 0)));
    }
    return logProb;
  }

  /**
   * Returns a vector chosen at random according to this distribution.
   */
  public MatrixLib sampleVal(List args) {
    initParams(args);

    final int numBuckets = pi.length;
    double[] cdf = new double[numBuckets];
    cdf[0] = pi[0];
    for (int i = 1; i < numBuckets; i++) {
      cdf[i] = cdf[i - 1] + pi[i];
    }

    int[] result = new int[numBuckets];
    for (int i = 0; i < numBuckets; i++) {
      result[i] = 0;
    }

    for (int trial = 0; trial < numTrials; trial++) {
      double val = Util.random();
      int bucket;
      for (bucket = 0; bucket < numBuckets; bucket++) {
        if (val <= cdf[bucket]) {
          break;
        }
      }
      result[bucket] += 1;
    }

    // Convert to Jama (nasty).
    double[][] doubleResult = new double[numBuckets][1];
    for (int i = 0; i < numBuckets; i++) {
      doubleResult[i][0] = result[i];
    }
    return MatrixFactory.fromArray(doubleResult);
  }

  private void initParams(List args) {
    int argidx = 0;
    if (expectTrialsAsArg) {
      Object obj = args.get(argidx);
      if (!(obj instanceof Number)) {
        throw new IllegalArgumentException(
            "expected first arg to be number numTrials");
      }
      this.numTrials = ((Number) obj).intValue();
      if (numTrials < 0) {
        throw new IllegalArgumentException(
            "Multinomial expects non-negative integer as the numTrial argument.");
      }
      argidx++;
    }
    if (expectWeightAsArg) {
      Object objectPi = args.get(argidx);
      double[] nativePi;
      if (objectPi instanceof MatrixLib) {
        MatrixLib pi = (MatrixLib) objectPi;
        if (pi.numCols() == 1) {
          nativePi = new double[pi.numRows()];
          for (int i = 0; i < pi.numRows(); i++) {
            nativePi[i] = pi.elementAt(i, 0);
          }
        } else if (pi.numRows() == 1) {
          nativePi = new double[pi.numCols()];
          for (int i = 0; i < pi.numCols(); i++) {
            nativePi[i] = pi.elementAt(0, i);
          }
        } else {
          throw new IllegalArgumentException(
              "expect either a row vector or column vector");
        }
      } else if (objectPi instanceof ArrayList) {
        ArrayList<?> arrayPi = (ArrayList<?>) objectPi;
        int size = arrayPi.size();
        nativePi = new double[size];
        for (int i = 0; i < size; i++)
          nativePi[i] = (Double) arrayPi.get(i);
      } else {
        throw new IllegalArgumentException(
            "expected second arg to be array of reals; got " + objectPi
                + " instead, which is of type " + objectPi.getClass().getName());
      }
      normalizeWeight(nativePi);
    }
  }

  private void normalizeWeight(double[] pi) {
    this.pi = pi;
    double sum = 0;
    for (int i = 0; i < pi.length; i++) {
      if (pi[i] < 0) {
        throw new IllegalArgumentException("Probability " + pi[i]
            + " for element " + i + " is negative.");
      }
      sum += pi[i];
    }
    if (sum < 1e-9) {
      throw new IllegalArgumentException("Probabilities sum to approx zero");
    }
    for (int i = 0; i < pi.length; i++) {
      this.pi[i] /= sum;
    }
  }

  private int numTrials;
  private double[] pi;
  private boolean expectTrialsAsArg;
  private boolean expectWeightAsArg;

  /*
   * (non-Javadoc)
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
   * <code>P</code> to method parameter p if distribution parameter
   * <code>P</code> has not previously been set.
   * 
   * @param n
   *          distribution parameter <code>N</code>, representing the number of
   *          categories. Must be nonnegative. Can be set multiple times.
   * 
   * @param p
   *          distribution parameter <code>P</code>, representing the row vector
   *          of unnormalized probabilities. Can only be set once.
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
      if (this.hasP) {
        throw new IllegalArgumentException(
            "Can only set the row vector of probabilities for a multinomial distribution once");
      }
      if (p.numRows() != 1 || p.numCols() == 0) {
        throw new IllegalArgumentException(
            "The row vector p passed in is not a row vector");
      }
      this.p = initializeProbabilityVector(p);
      this.k = this.p.length;
      this.hasP = true;
    }
  }

  /**
   * Precondition: p is a row vector
   * 
   * @param p
   * @returns an array of normalized probabilities from the row vector p.
   */
  private static double[] initializeProbabilityVector(MatrixLib p) {
    double[] pi = new double[p.numCols()];
    double sum = 0.0;
    for (int i = 0; i < p.numCols(); i++) {
      double ele = p.elementAt(0, i);
      if (ele < 0) {
        throw new IllegalArgumentException("Probability " + ele
            + " for element " + i + " is negative.");
      }
      sum += p.elementAt(0, i);
    }
    if (sum < 1e-9) {
      throw new IllegalArgumentException("Probabilities sum to approx zero");
    }
    for (int i = 0; i < p.numCols(); i++) {
      pi[i] = p.elementAt(0, i) / sum;
    }
    return pi;
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
    if (value.numRows() != 1 || value.numCols() != this.k) {
      throw new IllegalArgumentException(
          "The matrix provided is of the incorrect dimensions. Expecting a 1 by "
              + this.k + " row vector but instead got a matrix of dimension "
              + value.numRows() + " by " + value.numCols());
    }
    double sum = 0.0;
    for (int i = 0; i < k; i++) {
      double element = value.elementAt(0, i);
      if (element < 0 || element % 1 != 0.0) {
        return 0.0; // Number of successes for a particular category is negative
                    // or not an integer
      }
      sum += element;
    }
    if (sum != n) {
      return 0.0; // N != the sum of values
    }
    double prob = Util.factorial(n);
    for (int i = 0; i < k; i++) {
      prob *= Math.pow(p[i], value.elementAt(i, 0));
      prob /= Util.factorial((int) Math.round(value.elementAt(i, 0)));
      // FIXME: It would be better if we could take the param as an array
      // of ints, so we don't have to worry about rounding.
    }
    return prob;
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
    if (value.numRows() != 1 || value.numCols() != this.k) {
      throw new IllegalArgumentException(
          "The matrix provided is of the incorrect dimensions. Expecting a 1 by "
              + this.k + " row vector but instead got a matrix of dimension "
              + value.numRows() + " by " + value.numCols());
    }
    double sum = 0.0;
    for (int i = 0; i < k; i++) {
      double element = value.elementAt(0, i);
      if (element < 0 || element % 1 != 0.0) {
        return Double.NEGATIVE_INFINITY; // Number of successes for a particular
                                         // category is negative or not an
                                         // integer
      }
      sum += element;
    }
    if (sum != n) {
      return 0.0; // N != the sum of values
    }
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

  private int n;
  private boolean hasN;
  private double[] p;
  private boolean hasP;
  private int k; // the number of categories; dimension of p
}
