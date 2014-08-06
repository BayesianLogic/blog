/**
 * 
 */
package blog.distrib;

import java.util.HashSet;
import java.util.Set;

import blog.common.Util;
import blog.common.numerical.MatrixLib;

/**
 * The discrete distribution generates values in 0 ... k-1, with p.m.f. p(x) =
 * P_{x} where <code>P</code> is a distribution parameter that is a MatrixLib
 * row vector of probabilities. It is basically a simplified case of a
 * Multinomial distribution with <code>N = 1</code>.
 * 
 * @author cgioia
 * @since Jun 16, 2014
 * 
 */
public class Discrete implements CondProbDistrib {

  /*
   * set parameters for discrete distribution
   * 
   * @param params
   * 
   * An array of the form [MatrixLib]
   * <ul>
   * <li><code>P</code> (MatrixLib in the form of a column vector)
   * </ul>
   * 
   * @see blog.distrib.CondProbDistrib#setParams(java.lang.Object[])
   */
  @Override
  public void setParams(Object[] params) {
    if (params.length != 1) {
      throw new IllegalArgumentException("expecting one parameter");
    }
    setParams((MatrixLib) params[0]);
  }

  /**
   * If the method parameter value is non-null and is a column vector, then set
   * the
   * distribution parameter <code>P</code> to value.
   */
  public void setParams(MatrixLib value) {
    if (value != null) {
      if (value.numCols() != 1 || value.numRows() == 0) {
        throw new IllegalArgumentException(
            "The argument passed into setParams is not a column vector");
      }
      initializeProbabilityVector(value);
      this.hasP = true;
      this.finiteSupport.clear();
      for (int i = 0; i < k; i++) {
        if (!Util.closeToZero(this.p[i])) {
          finiteSupport.add(i);
        }
      }
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
    double[] pi = new double[p.numRows()];
    this.logP = new double[p.numRows()];
    double sum = 0.0;
    for (int i = 0; i < p.numRows(); i++) {
      double ele = p.elementAt(i, 0);
      if (ele < 0) {
        throw new IllegalArgumentException("Probability " + ele
            + " for element " + i + " is negative.");
      }
      sum += p.elementAt(i, 0);
    }
    if (Util.closeToZero(sum)) {
      throw new IllegalArgumentException("Probabilities sum to approx zero");
    }
    // normalization
    for (int i = 0; i < p.numRows(); i++) {
      pi[i] = p.elementAt(i, 0) / sum;
      this.logP[i] = Math.log(pi[i]);
    }
    this.p = pi;
    this.k = this.p.length;

    // Precompute the CDF of P for Sampling
    this.pCDF = new double[k];
    pCDF[0] = this.p[0];
    for (int i = 1; i < k; i++) {
      this.pCDF[i] = pCDF[i - 1] + this.p[i];
    }
  }

  private void checkHasParams() {
    if (!this.hasP) {
      throw new IllegalArgumentException(
          "parameter P (row vector) not provided");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#getProb(java.lang.Object)
   */
  @Override
  public double getProb(Object value) {
    return getProb((Integer) value);
  }

  /**
   * Considering that our distribution parameter <code>P</code> is now a
   * normalized row vector of probabilities consisting of elements P_0, P_1,
   * ... , P_K-1, returns the probability that the Multinomial distribution
   * takes on the outcome corresponding to index <code>value</code>.
   * 
   * @param value
   */
  public double getProb(Integer value) {
    checkHasParams();
    if (value < 0 || value >= this.k) {
      return 0.0;
    }
    return p[value];
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#getLogProb(java.lang.Object)
   */
  @Override
  public double getLogProb(Object value) {
    return getLogProb((Integer) value);
  }

  public double getLogProb(Integer value) {
    checkHasParams();
    if (value < 0 || value >= this.k) {
      return Double.NEGATIVE_INFINITY;
    }
    return logP[value];
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

  /**
   * Samples a value from the multinomial. Say the unnormalized probabilities of
   * <code>P</code> are P_0, ... P_K-1. Returns the index <code>i</code>
   * corresponding to outcome with probability P_i.
   */
  public Integer sample_value() {
    checkHasParams();
    double val = Util.random();
    for (int i = 0; i < k; i++) {
      if (val <= pCDF[i]) {
        return i;
      }
    }
    return k;
  }

  @Override
  public Set getFiniteSupport() {
    checkHasParams();
    return finiteSupport;
  }

  private double[] p;
  private double[] logP;
  private double[] pCDF;
  private boolean hasP;
  private int k; // the number of categories; dimension of p
  private Set finiteSupport = new HashSet();
}
