/*
 * Copyright (c) 2012, Regents of the University of California
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import blog.common.numerical.MatrixFactory;
import blog.common.numerical.MatrixLib;

/**
 * A Dirichlet distribution with shape parameter vector a, defined by
 * f(x1, x2, ... xn) = 1/Z * (x1^(a1-1) * x2^(a2-1) * ... * xn^(an-1),
 * where \sum(xi) = 1 and Z = \prod(Gamma(ai)) / Gamma(\sum(ai)).
 * 
 * Note that this is a generalization of the Beta distribution: while
 * the Beta generates a parameter for the Bernoulli process (Bernoulli
 * and Binomial distributions), the Dirichlet generates parameters for
 * the Categorical process (Categorical and Multinomial distributions).
 */
public class Dirichlet extends AbstractCondProbDistrib {

  private Double[] alpha;
  private Gamma[] gammas;

  /**
   * Constructs a Dirichlet distribution with the given parameters.
   * 
   * @param a
   *          list of parameters for the distribution
   */
  public Dirichlet(List<Double> params) {
    alpha = new Double[params.size()];
    params.toArray(alpha);
    initGammas();
  }

  /**
   * Constructs a Dirichlet distribution with the given dimension and
   * parameter value for all dimensions
   * 
   * @param the
   *          dimension of the distribution
   * @param the
   *          value for all parameters of this distribution
   */
  public Dirichlet(int dimension, double paramVal) {
    alpha = new Double[dimension];
    Arrays.fill(alpha, paramVal);
    initGammas();
  }

  /**
   * Helper for constructor to construct needed gamma functions
   */
  private void initGammas() {
    int numParams = alpha.length;
    gammas = new Gamma[numParams];
    for (int i = 0; i < numParams; i++) {
      gammas[i] = new Gamma(alpha[i], 1);
    }
  }

  /**
   * Helper for sampleVal() to reconstruct gamma functions
   */
  private void reconstructGammas(int vec_size) {
    int numParams = vec_size;
    gammas = new Gamma[numParams];
    for (int i = 0; i < numParams; i++) {
      gammas[i] = new Gamma(alpha[0], 1);
    }
  }

  /**
   * Returns the probability of a vector of values from this distribution.
   */
  public double getProb(List args, Object childValue) {
    if (!(childValue instanceof MatrixLib)) {
      throw new IllegalArgumentException("Dirichlet distribution"
          + "requires MatrixLib as argument, not " + childValue.getClass()
          + ".");
    }

    MatrixLib mat = (MatrixLib) childValue;
    if (mat.numRows() != 1 || mat.numCols() == 0) {
      throw new IllegalArgumentException("Dirichlet distribution"
          + "requires nonempty vector of Numbers as argument.");
    }

    double prob = 1.0;
    for (int i = 0; i < mat.numCols(); i++) {
      double count_term = (args.size() != 0) ? alpha[0] : alpha[i];
      double x = mat.elementAt(0, i);
      prob *= Math.pow(x, count_term - 1);
    }
    prob /= normalize(alpha);

    return prob;
  }

  /**
   * Returns the log of the probability of a vector of values from this
   * distribution.
   */
  public double getLogProb(List args, Object childValue) {
    if (!(childValue instanceof MatrixLib)) {
      throw new IllegalArgumentException("Dirichlet distribution"
          + "requires MatrixLib as argument, not " + childValue.getClass()
          + ".");
    }

    MatrixLib mat = (MatrixLib) childValue;
    if (mat.numRows() != 1 || mat.numCols() == 0) {
      throw new IllegalArgumentException("Dirichlet distribution"
          + "requires nonempty vector of Numbers as argument.");
    }

    double prob = 0.0;
    for (int i = 0; i < mat.numCols(); i++) {
      double count_term = (args.size() != 0) ? alpha[0] : alpha[i];
      double x = mat.elementAt(0, i);
      prob += Math.log(x) * (count_term - 1);
    }
    prob -= Math.log(normalize(alpha));

    return prob;
  }

  /**
   * Returns a list of doubles sampled from this distribution.
   */
  public Object sampleVal(List args) {
    double sum = 0.0;
    int vec_size = alpha.length;
    if (args.size() != 0) {
      vec_size = (Integer) args.get(0);
      reconstructGammas(vec_size);
    }

    double[][] samples = new double[1][vec_size];
    List<Object> dummy = new ArrayList<Object>();
    for (int i = 0; i < vec_size; i++) {
      Gamma component = gammas[i];
      double sample = (Double) component.sampleVal(dummy);
      sum += sample;
      samples[0][i] = sample;
    }

    for (int i = 0; i < vec_size; i++) {
      samples[0][i] /= sum;
    }
    return MatrixFactory.fromArray(samples);
  }

  /**
   * Computes the normalization constant for a Dirichlet distribution with
   * the given parameters.
   * 
   * @param a
   *          list of parameters of a Dirichlet distribution
   * @return the normalization constant for such a distribution
   */
  public static final double normalize(Double[] params) {
    double denom = 0.0;
    double numer = 1.0;

    for (double param : params) {
      numer *= Gamma.gamma(param);
      denom += param;
    }
    denom = Gamma.gamma(denom);

    return numer / denom;
  }

  public String toString() {
    return getClass().getName();
  }

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
