package blog.distrib;

import java.util.List;

import blog.common.Util;

/**
 * Uniform distribution over a range of real numbers [lower, upper).
 */
public class UniformReal implements CondProbDistrib {
  public UniformReal() {
  }

  public UniformReal(double lower, double upper) {
    setParams(lower, upper);
  }

  public void setParams(List<Object> params) {
    if (params.size() != 2) {
      throw new IllegalArgumentException("expected two params: lower and upper");
    }
    setParams((Double) params.get(0), (Double) params.get(1));
  }

  public void setParams(Double lower, Double upper) {
    if (lower != null) {
      this.lower = lower;
      this.hasLower = true;
    }
    if (upper != null) {
      this.upper = upper;
      this.hasUpper = true;
    }
    if (this.hasLower && this.hasUpper && this.lower >= this.upper) {
      throw new IllegalArgumentException("lower >= upper");
    }
  }

  private void checkHasParams() {
    if (!hasLower) {
      throw new IllegalArgumentException("lower not provided");
    }
    if (!hasUpper) {
      throw new IllegalArgumentException("upper not provided");
    }
  }

  public double getProb(Object value) {
    return getProb(((Double) value).doubleValue());
  }

  public double getProb(double value) {
    checkHasParams();
    if ((value >= lower) && (value < upper)) {
      return 1.0 / (upper - lower);
    } else {
      return 0.0;
    }
  }

  public double getLogProb(Object value) {
    return getLogProb(((Double) value).doubleValue());
  }

  public double getLogProb(double value) {
    return Math.log(getProb(value));
  }

  public Object sampleVal() {
    return sampleValue();
  }

  public double sampleValue() {
    checkHasParams();
    // rely on the fact that Util.random() returns a value in [0, 1)
    double x = lower + (Util.random() * (upper - lower));
    return x;
  }

  public double getLower() {
    return lower;
  }

  public double getUpper() {
    return upper;
  }

  public String toString() {
    return "Uniform(" + lower + ", " + upper + ")";
  }

  private boolean hasLower;
  private boolean hasUpper;

  private double lower;
  private double upper;
}
