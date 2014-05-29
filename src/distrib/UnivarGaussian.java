package distrib;

import java.util.Random;

public class UnivarGaussian {

  public UnivarGaussian() {
    this(new Random());
  }

  public UnivarGaussian(Random rng_) {
    rng = rng_;
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
    double U = rng.nextDouble();
    double V = rng.nextDouble();
    return (mean + (sqrtVariance * Math.sin(2 * Math.PI * V) * Math
        .sqrt((-2 * Math.log(U)))));
  }

  private Random rng;
  private boolean hasMean;
  private boolean hasVariance;

  protected double mean;
  protected double variance;
  protected double sqrtVariance;
  private double normConst;
  private double logNormConst;

  private static final double ROOT_2PI = Math.sqrt(2 * Math.PI);
  private static final double LOG_ROOT_2PI = 0.5 * (Math.log(2) + Math
      .log(Math.PI));
}
