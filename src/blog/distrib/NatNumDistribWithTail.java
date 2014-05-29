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

import java.util.List;

import Jama.Matrix;

/**
 * A mixture of an explicit distribution over the first k natural numbers
 * 0,...,(k-1), and a geometric distribution over the numbers greater than or
 * equal to k. This distribution takes three parameters: a column vector of k
 * probabilities for the first k numbers, the probability lambda that the value
 * is less than k, and the success parameter alpha of the geometric
 * distribution.
 */
public class NatNumDistribWithTail extends AbstractCondProbDistrib {
  /**
   * Creates a NatNumDistribWithTail with the given array of probabilities for
   * the categorical distribution, and the given lambda and alpha values. k is
   * set to the size of the probability array.
   * 
   * @param pi
   *          an array of double values specifying a probability distribution
   *          over the first pi.length natural numbers
   * 
   * @param lambda
   *          the probability P(X &lt; pi.length)
   * 
   * @param alpha
   *          the success probability of the geometric distribution: P(X &gt;=
   *          n+1 | X &gt;= n) for n &gt;= pi.length
   */
  public NatNumDistribWithTail(double[] pi, double lambda, double alpha) {
    k = pi.length;
    prefixDistrib = new Categorical(pi);
    mixDistrib = new BooleanDistrib(lambda);
    geometric = new Geometric(alpha);
  }

  /**
   * Creates a new NatNumDistribWithTail with the following three parameters:
   * <ol>
   * <li>a k-by-1 matrix specifying a probability distribution over the first k
   * natural numbers
   * 
   * <li>the probability of generating a number less than k
   * 
   * <li>the success probability of the geometric distribution for values
   * greater than or equal to k
   * </ol>
   */
  public NatNumDistribWithTail(List params) {
    if (params.size() != 3) {
      throw new IllegalArgumentException(
          "NatNumDistribWithTail expects three parameters: the "
              + "distribution over small numbers, the probability of "
              + "using that distribution, and the success probability "
              + "for a geometric distribution over larger numbers.");
    }

    if ((!(params.get(0) instanceof Matrix))
        || (((Matrix) params.get(0)).getColumnDimension() != 1)) {
      throw new IllegalArgumentException(
          "First parameter to NatNumDistribWithTail should be a "
              + "column vector.");
    }
    Matrix m = (Matrix) params.get(0);
    k = m.getRowDimension();
    double[] probs = m.getColumnPackedCopy();
    prefixDistrib = new Categorical(probs);

    if (!(params.get(1) instanceof Number)) {
      throw new IllegalArgumentException(
          "Second parameter to NatNumDistribWithTail should be a "
              + "number (the probability of using the explicit distrib).");
    }
    mixDistrib = new BooleanDistrib(((Number) params.get(1)).doubleValue());

    if (!(params.get(2) instanceof Number)) {
      throw new IllegalArgumentException(
          "Third parameter to NatNumDistribWithTail should be a "
              + "number (the success prob for the geometric distrib).");
    }
    geometric = new Geometric(((Number) params.get(2)).doubleValue());
  }

  /**
   * Returns the probability of a non-negative integer n under this
   * distribution. Throws an exception if n is negative.
   */
  public double getProb(int n) {
    if (n < k) {
      return mixDistrib.getProb(true) * prefixDistrib.getProb(n);
    }
    return mixDistrib.getProb(false) * geometric.getProb(n - k);
  }

  /**
   * Returns the log probability of a non-negative integer n under this
   * distribution. Throws an exception if n is negative.
   */
  public double getLogProb(int n) {
    if (n < k) {
      return mixDistrib.getLogProb(true) + prefixDistrib.getLogProb(n);
    }
    return mixDistrib.getLogProb(false) + geometric.getLogProb(n - k);
  }

  /**
   * Returns the probability of the given value, which should be a non-negative
   * Integer. Expects no arguments.
   */
  public double getProb(List args, Object childValue) {
    if (!args.isEmpty()) {
      throw new IllegalArgumentException(
          "NatNumDistribWithTail expects no arguments.");
    }

    if (!(childValue instanceof Integer)) {
      throw new IllegalArgumentException(
          "NatNumDistribWithTail defines distribution over objects "
              + "of class Integer, not " + childValue.getClass());
    }

    return getProb(((Integer) childValue).intValue());
  }

  /**
   * Returns a sample from this distribution.
   */
  public int sampleVal_() {
    boolean usePrefix = mixDistrib.sampleVal_();
    if (usePrefix) {
      // X < k
      return prefixDistrib.sampleVal_();
    } else {
      // X >= k
      return k + geometric.sampleVal_();
    }
  }

  /**
   * Returns an Integer sampled from this distribution. Expects no arguments.
   */
  public Object sampleVal(List args) {
    if (!args.isEmpty()) {
      throw new IllegalArgumentException(
          "NatNumDistribWithTail expects no arguments.");
    }

    return new Integer(sampleVal_());
  }

  int k;
  BooleanDistrib mixDistrib;
  Categorical prefixDistrib;
  Geometric geometric;

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#setParams(java.util.List)
   */
  @Override
  public void setParams(List<Object> params) {
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
