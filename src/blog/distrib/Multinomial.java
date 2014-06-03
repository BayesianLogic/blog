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
import blog.model.MatrixSpec;

/**
 * Multinomial distribution.
 * Multinomial distribution accepts two arguments,
 * <num of trials>, <weight matrix/list>, either or both can be random.
 * See https://en.wikipedia.org/wiki/Multinomial_distribution
 */
public class Multinomial extends AbstractCondProbDistrib {

  /**
   * Multinomial distribution accepts two arguments,
   * <num of trials>, <weight matrix/list>, either or both can be random.
   * 
   * @param params
   */
  public Multinomial(List params) {
    if (params.size() == 2) {
      expectTrialsAsArg = true;
      expectWeightAsArg = true;
      initParams(params);
      expectTrialsAsArg = false;
      expectWeightAsArg = false;
    } else if (params.size() == 1) {
      Object obj = params.get(0);
      if (obj instanceof Integer) {
        expectTrialsAsArg = true;
        expectWeightAsArg = false;
        initParams(params);
        expectTrialsAsArg = false;
        expectWeightAsArg = true;
      } else {
        expectTrialsAsArg = false;
        expectWeightAsArg = true;
        initParams(params);
        expectTrialsAsArg = true;
        expectWeightAsArg = false;
      }
    } else {
      expectTrialsAsArg = true;
      expectWeightAsArg = true;
    }
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
      if (objectPi instanceof MatrixSpec) {
        objectPi = ((MatrixSpec) objectPi).getValueIfNonRandom();
      }
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
