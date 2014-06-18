package blog.distrib;

import blog.common.Util;

/**
 * Univariate Gaussian distribution with a given <code>mean</code> and
 * <code>variance</code>.
 * 
 * @author cgioia
 * @since June 18, 2014
 */
public class UnivarGaussian implements CondProbDistrib {

  /**
   * Public constructor intended for use by BLOG Engine.
   */
  public UnivarGaussian() {
  }

  /**
   * This constructor is not intended for public use.
   */
  private UnivarGaussian(double mean, double variance) {
    setParams(mean, variance);
  }

  /**
   * set Parameters for Gaussian distribution
   * 
   * @param params
   *          An array of two double
   *          <ul>
   *          <li>params[0]: <code>mean</code></li>
   *          <li>params[1]: <code>variance</code></li>
   *          </ul>
   *          
   * @see blog.distrib.CondProbDistrib#setParams(java.lang.Object[])
   */
  public void setParams(Object[] params) {
    if (params.length != 2) {
      throw new IllegalArgumentException(
          "expected two params: mean and variance");
    }
    setParams((Double) params[0], (Double) params[1]);
  }

  /**
   * If the method parameter mean is non-null, sets the distribution parameter
   * <code>mean</code> to the method parameter mean. Similarly for variance
   * assuming variance is strictly positive.
   */
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
      throw new IllegalArgumentException("mean of Gaussian not provided");
    }
    if (!hasVariance) {
      throw new IllegalArgumentException("variance of Gaussian not provided");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#getProb(java.lang.Object)
   */
  public double getProb(Object value) {
    return getProb(((Double) value).doubleValue());
  }

  /**
   * Returns the probability of <code>x</code>.
   */
  public double getProb(double x) {
    checkHasParams();
    return (Math.exp(-Math.pow((x - mean), 2) / (2 * variance)) / normConst);
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#getLogProb(java.lang.Object)
   */
  public double getLogProb(Object value) {
    return getLogProb(((Double) value).doubleValue());
  }

  /** Returns the log probability of <code>x</code>. */
  public double getLogProb(double x) {
    checkHasParams();
    return (-(x - mean) * (x - mean) / (2 * variance)) - logNormConst;
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#sampleVal()
   */
  public Object sampleVal() {
    return sample_value();
  }

  /**
   * Samples a value from the Univariate gaussian.
   * Intended for human use.
   */
  public double sample_value() {
    checkHasParams();
    double U = Util.random();
    double V = Util.random();
    return (mean + (sqrtVariance * Math.sin(2 * Math.PI * V) * Math
        .sqrt((-2 * Math.log(U)))));
  }

  @Override
  public String toString() {
    return "UnivarGaussian(" + mean + ", " + variance + ")";
  }

  /** The Standard Gaussian (mean = 0, variance = 1). */
  public static final UnivarGaussian STANDARD = new UnivarGaussian(0, 1);

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
