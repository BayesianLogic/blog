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

import java.util.HashMap;
import java.util.Map;

import blog.common.Util;

/**
 * Categorical Distribution takes a distribution parameter <code>map</code>,
 * which is a map from objects to numbers. The number corresponding to each
 * object represents the probability of that object occurring.
 */
public class Categorical implements CondProbDistrib {

  /**
   * set parameters for categorical distribution
   * 
   * @param params
   *          An array of the form [Map<Object, Double>]
   *          <ul>
   *          <li>params[0]: <code>map</code> (Map<Object, Double>)</li>
   *          </ul>
   * 
   * @see blog.distrib.CondProbDistrib#setParams(java.lang.Object[])
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
      for (Map.Entry<Object, Double> entry : map.entrySet()) {
        double prob = entry.getValue();
        if (prob < 0) {
          throw new IllegalArgumentException("Probability " + prob
              + " for key " + entry.getKey().toString() + " is negative");
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
      for (Map.Entry<Object, Double> entry : map.entrySet()) {
        Object key = entry.getKey();
        Double value = entry.getValue();
        this.objects[count] = key;
        double prob = value / sum;
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

  @Override
  public String toString() {
    return getClass().getName();
  }

  private HashMap<Object, Double> map;
  private HashMap<Object, Double> logMap;
  private Object[] objects; // Ordered collection of objects
  private double[] cdfObjects; // CDF corresponding to objects
  private boolean hasMap;
}
