/**
 * 
 */
package blog.distrib;

import blog.common.Util;
import blog.common.numerical.MatrixLib;

/**
 * A Discrete Distribution takes a parameter <code>P</code> which is a MatrixLib
 * row vector of probabilities. It is basically a simplified case of Multinomial
 * distribution with <code>N = 1</code>. Consider the elements of <code>P</code>
 * to be P_0, P_1, ..,P_K-1.
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
   * An array of one MatrixLib
   * params[0]: <code>P</code> (MatrixLib in the form of a row vector)
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
   * If the method parameter value is non-null and is a row vector, then set the
   * distribution parameter <code>P</code> to value.
   */
  public void setParams(MatrixLib value) {
    if (value != null) {
      if (value.numRows() != 1 || value.numCols() == 0) {
        throw new IllegalArgumentException(
            "The argument passed into setParams passed in is not a row vector");
      }
      initializeProbabilityVector(value);
      this.hasP = true;
    }
  }

  /**
   * Precondition: p is a row vector
   * 
   * Sets instance variable p to a normalized array of probabilities
   * Sets pCDF to the CDF of p
   * 
   * @param p
   */
  private void initializeProbabilityVector(MatrixLib p) {
    double[] pi = new double[p.numCols()];
    this.logP = new double[p.numCols()];
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
    // normalization
    for (int i = 0; i < p.numCols(); i++) {
      pi[i] = p.elementAt(0, i) / sum;
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
    if (value < 0 || value > this.k) {
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
    if (value < 0 || value > this.k) {
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

  private double[] p;
  private double[] logP;
  private double[] pCDF;
  private boolean hasP;
  private int k; // the number of categories; dimension of p

}
