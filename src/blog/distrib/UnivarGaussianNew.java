package blog.distrib;

import java.util.List;

import blog.common.Util;

public class UnivarGaussianNew implements Distrib {

  // Glue code for Distrib interface:

  public void setParams(List<Object> params) {
    if (params.size() != 2) {
      throw new IllegalArgumentException(
          "expected two params: mean and variance");
    }
    setParams((Double) params.get(0), (Double) params.get(1));
  }

  public double getProb(Object value) {
    return getProb((Double) value);
  }

  public double getLogProb(Object value) {
    return getLogProb((Double) value);
  }

  public Object sampleVal() {
    return sampleValue();
  }

  // End of glue code.

  public UnivarGaussianNew() {
  }

  public UnivarGaussianNew(Double mean_, Double variance_) {
    setParams(mean_, variance_);
  }

  public void setParams(Double mean_, Double variance_) {
    if (mean_ != null) {
      mean = mean_;
      hasMean = true;
    }

    if (variance_ != null) {
      if (variance <= 0) {
        throw new IllegalArgumentException("variance must be positive");
      }
      variance = variance_;
      sqrtVariance = Math.sqrt(variance);
      normConst = sqrtVariance * ROOT_2PI;
      logNormConst = (0.5 * Math.log(variance)) + LOG_ROOT_2PI;
      hasVariance = true;
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

  public double getProb(double x) {
    checkHasParams();
    return (Math.exp(-Math.pow((x - mean), 2) / (2 * variance)) / normConst);
  }

  public double getLogProb(double x) {
    checkHasParams();
    return (-(x - mean) * (x - mean) / (2 * variance)) - logNormConst;
  }

  public double sampleValue() {
    checkHasParams();
    double U = Util.random();
    double V = Util.random();
    return (mean + (sqrtVariance * Math.sin(2 * Math.PI * V) * Math
        .sqrt((-2 * Math.log(U)))));
  }

  private boolean hasMean;
  private boolean hasVariance;

  private double mean;
  private double variance;
  private double sqrtVariance;
  private double normConst;
  private double logNormConst;

  private static final double ROOT_2PI = Math.sqrt(2 * Math.PI);
  private static final double LOG_ROOT_2PI = 0.5 * (Math.log(2) + Math
      .log(Math.PI));
}
