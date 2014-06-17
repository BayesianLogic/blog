/*
 * Copyright (c) 2005, 2006, Regents of the University of California
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import blog.common.Util;
import blog.common.numerical.MatrixLib;
import blog.model.Model;

/**
 * Categorical Distribution takes a distribution parameter <code>map</code>,
 * which is a map from objects to numbers. The number corresponding to each
 * object represents the probability of that object occurring.
 */
public class Categorical extends AbstractCondProbDistrib {

  /**
   * Returns an index sampled from this distribution.
   * 
   * @throws IllegalStateException
   *           if the probability vector was not specified when this
   *           distribution was constructed
   */
  public int sampleVal_() {
    if (expectProbsAsArg) {
      throw new IllegalStateException(
          "Categorical distribution was constructed without a "
              + "probability vector.");
    }

    return Util.sampleWithProbs(probs);
  }

  /**
   * Returns a value sampled from the given child type according to this
   * distribution. If this distribution was constructed with a probability
   * vector, then no arguments are expected. Otherwise, there should be one
   * argument, namely a column vector of probabilities.
   * 
   * @return a guaranteed object of the given type whose index is sampled
   *         according to this distribution, or Model.NULL if the index sampled
   *         is beyond the end of the guaranteed object list for the given type
   * 
   * @throws IllegalArgumentException
   *           if the probability vector was specified at construction and
   *           <code>args</code> is non-empty, or if the probability vector was
   *           not specified and <code>args</code> is empty
   */
  public Object sampleVal(List args) {
    ensureProbsInited(args);

    int index = Util.sampleWithProbs(probs);
    Object value = values[index];

    if (value == null) {
      // make list so we can print the probabilities easily
      List probList = new ArrayList();
      for (int i = 0; i < probs.length; ++i) {
        probList.add(new Double(probs[i]));
      }
      System.err.println("Warning: distribution does not sum to 1");
      value = Model.NULL;
    }
    return value;
  }

  private void ensureProbsInited(List args) {
    if (expectProbsAsArg) {
      if (args.isEmpty()) {
        throw new IllegalArgumentException(
            "Arguments to Categorical CPD should consist of a "
                + "probability vector, since the probabilities were not "
                + "specified as CPD parameters.");
      }

      Object arg = args.get(0);
      if (arg instanceof MatrixLib) {
        MatrixLib m = (MatrixLib) args.get(0);
        probs = new double[m.numCols()];
        values = new Object[m.numCols()];
        for (int i = 0; i < probs.length; ++i) {
          probs[i] = m.elementAt(0, i);
          values[i] = Integer.valueOf(i);
        }
      } else if (arg instanceof Map) {
        Map<Object, Number> map = (Map<Object, Number>) arg;
        int entrysize = map.size();
        probs = new double[entrysize];
        values = new Object[entrysize];
        int termIndex = 0;
        for (Map.Entry<Object, Number> entry : map.entrySet()) {
          probs[termIndex] = entry.getValue().doubleValue();
          values[termIndex] = entry.getKey();
          termIndex++;
        }
      } else {
        throw new IllegalArgumentException(
            "Argument to Categorical CPD should be a row "
                + "vector of probabilities, or a map not: " + args.get(0));
      }
      normalizeProb();
    } else if (args != null) {
      if (!args.isEmpty()) {
        throw new IllegalArgumentException(
            "Categorical CPD expects no arguments (probabilities "
                + "were specified as CPD parameters).");
      }
    }
  }

  boolean expectProbsAsArg = false;
  private double[] probs;
  private Object[] values;

  /**
   * set parameters for categorical distribution
   * 
   * @param params
   *          An array of one Map<Object, Double>
   *          params[0]: <code>map</code> (Map from Object to Double)
   * 
   * @see blog.distrib.CondProbDistrib#setParams(java.util.List)
   */
  @Override
  public void setParams(Object[] params) {
    if (params.length != 1) {
      throw new IllegalArgumentException("expecting one parameter");
    }
    setParams((Map<Object, Double>) params[0]);
  }

  /**
   * If method parameter map is non-null, normalize the probabilities of the
   * values for each object, and set the distribution parameter <code>map</code>
   * to the method parameter map.
   */
  public void setParams(Map<Object, Double> map) {
    if (map != null) {
      if (map.size() == 0) {
        throw new IllegalArgumentException(
            "no elements within map for categorical distribution");
      }
      double sum = 0.0;
      for (Object key : map.keySet()) {
        double prob = map.get(key);
        if (prob < 0) {
          throw new IllegalArgumentException("Probability " + prob
              + " for key " + key.toString() + " is negative");
        }
        sum += prob;
      }
      if (sum < 1e-9) {
        throw new IllegalArgumentException(
            "probabilities sum to approximately zero");
      }
      this.map = new HashMap<Object, Double>();
      this.logMap = new HashMap<Object, Double>();
      this.objects = new Object[map.size()];
      this.cdfObjects = new double[map.size()];
      int count = 0;
      double cdf = 0.0;
      for (Object key : map.keySet()) {
        this.objects[count] = key;
        double prob = map.get(key) / sum;
        cdf += prob;
        this.cdfObjects[count] = cdf;
        this.map.put(key, prob);
        this.logMap.put(key, Math.log(prob));
        count += 1;
      }
      this.hasMap = true;
    }
  }

  private void checkHasParams() {
    if (!this.hasMap) {
      throw new IllegalArgumentException("parameter map not provided");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#getProb(java.lang.Object)
   */
  @Override
  public double getProb(Object value) {
    checkHasParams();
    Double prob = map.get(value);
    return prob != null ? prob : 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#getLogProb(java.lang.Object)
   */
  @Override
  public double getLogProb(Object value) {
    checkHasParams();
    Double logProb = logMap.get(value);
    return logProb != null ? logProb : Double.NEGATIVE_INFINITY;
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#sampleVal()
   */
  @Override
  public Object sampleVal() {
    checkHasParams();
    double val = Util.random();
    for (int i = 0; i < cdfObjects.length; i++) {
      if (val <= cdfObjects[i]) {
        return objects[i];
      }
    }
    return objects[objects.length - 1];
  }

  private HashMap<Object, Double> map;
  private HashMap<Object, Double> logMap;
  private Object[] objects; // Ordered collection of objects
  private double[] cdfObjects; // CDF corresponding to objects
  private boolean hasMap;
}
