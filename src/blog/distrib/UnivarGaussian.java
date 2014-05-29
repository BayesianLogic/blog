package blog.distrib;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import blog.common.Util;

/**
 * Univariate Gaussian distribution with a given mean and variance.
 */
public class UnivarGaussian implements CondProbDistrib {
  public UnivarGaussian() {
  }

  public UnivarGaussian(double mean, double variance) {
    setParams(mean, variance);
  }

  public void setParams(List<Object> params) {
    if (params.size() != 2) {
      throw new IllegalArgumentException(
          "expected two params: mean and variance");
    }
    setParams((Double) params.get(0), (Double) params.get(1));
  }

  public void setParams(Double mean, Double variance) {
    if (mean != null) {
      this.mean = mean;
      this.hasMean = true;
    }

    if (variance != null) {
      if (variance <= 0) {
        throw new IllegalArgumentException("variance must be positive");
      }
      this.variance = variance;
      this.hasVariance = true;
      this.sqrtVariance = Math.sqrt(variance);
      this.normConst = sqrtVariance * ROOT_2PI;
      this.logNormConst = (0.5 * Math.log(variance)) + LOG_ROOT_2PI;
    }
  }

  private void checkHasParams() {
    if (!hasMean) {
      throw new IllegalArgumentException("mean not provided");
    }
    if (!hasVariance) {
      throw new IllegalArgumentException("variance not provided");
    }
  }

  public double getProb(Object value) {
    return getProb(((Double) value).doubleValue());
  }

  public double getProb(double x) {
    checkHasParams();
    return (Math.exp(-Math.pow((x - mean), 2) / (2 * variance)) / normConst);
  }

  public double getLogProb(Object value) {
    return getLogProb(((Double) value).doubleValue());
  }

  public double getLogProb(double x) {
    checkHasParams();
    return (-(x - mean) * (x - mean) / (2 * variance)) - logNormConst;
  }

  public Object sampleVal() {
    return sampleValue();
  }

  public double sampleValue() {
    checkHasParams();
    double U = Util.random();
    double V = Util.random();
    return (mean + (sqrtVariance * Math.sin(2 * Math.PI * V) * Math
        .sqrt((-2 * Math.log(U)))));
  }

  public double getMean() {
    return mean;
  }

  public double getVariance() {
    return variance;
  }

  public String toString() {
    return "UnivarGaussian(" + mean + ", " + variance + ")";
  }

  public static final UnivarGaussian STANDARD = new UnivarGaussian(0, 1);

  /**
   * Returns a Gaussian distribution corresponding to the product of this and
   * another Gaussian distribution.
   */
  public UnivarGaussian product(UnivarGaussian another) {
    double sumOfSigmaSquares = variance + another.variance;
    return new UnivarGaussian((mean * another.variance + another.mean
        * variance)
        / sumOfSigmaSquares, (variance * another.variance) / sumOfSigmaSquares);
  }

  /**
   * Returns the product of a set of UnivarGaussians, returning
   * <code>null</code> if set is empty.
   */
  public static UnivarGaussian product(Collection<UnivarGaussian> gaussians) {
    if (gaussians.size() == 0)
      return null;
    Iterator<UnivarGaussian> gaussiansIt = gaussians.iterator();
    UnivarGaussian result = gaussiansIt.next();
    while (gaussiansIt.hasNext()) {
      result = result.product(gaussiansIt.next());
    }
    return result;
  }

  /**
   * Returns a Gaussian representing the posterior of the mean of this Gaussian
   * (but ignoring its currently set mean) given a value of its domain.
   */
  public UnivarGaussian meanPosterior(double value) {
    return new UnivarGaussian(value, variance);
  }

  // Parameters:
  private boolean hasMean;
  private boolean hasVariance;
  protected double mean;
  protected double variance;

  // Precomputed stuff:
  protected double sqrtVariance;
  private double normConst;
  private double logNormConst;
  private static final double ROOT_2PI = Math.sqrt(2 * Math.PI);
  private static final double LOG_ROOT_2PI = 0.5 * (Math.log(2) + Math
      .log(Math.PI));
}
