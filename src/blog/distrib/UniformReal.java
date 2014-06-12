package blog.distrib;

import blog.common.Util;

/**
 * Uniform distribution over a range of real numbers [<code>lower</code>,
 * <code>upper</code>).
 */
public class UniformReal implements CondProbDistrib {

  /**
   * mapping for <code>params</code>:
   * <ul>
   * <li>params[0]:<code>lower</code></li>
   * <li>params[1]:<code>upper</code></li>
   * </ul>
   * 
   * @see blog.distrib.CondProbDistrib#setParams(java.lang.Object[])
   */
  public void setParams(Object[] params) {
    if (params.length != 2) {
      throw new IllegalArgumentException("expected two params: lower and upper");
    }
    setParams((Double) params[0], (Double) params[1]);
  }

  /**
   * If the method parameter lower is non-null, sets the distribution parameter
   * <code>lower</code> to lower. Similarly for upper. Once both parameters
   * <code>lower</code> and <code>upper</code> have been set, checks to see that
   * <code>lower</code> < <code>upper</code>.
   */
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

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#getProb(java.lang.Object)
   */
  public double getProb(Object value) {
    return getProb(((Double) value).doubleValue());
  }

  /**
   * Returns the probability of <code>value</code>.
   */
  public double getProb(double value) {
    checkHasParams();
    if ((value >= lower) && (value < upper)) {
      return 1.0 / (upper - lower);
    } else {
      return 0.0;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#getLogProb(java.lang.Object)
   */
  public double getLogProb(Object value) {
    return getLogProb(((Double) value).doubleValue());
  }

  /**
   * Returns the log probability of <code>value</code>.
   */
  public double getLogProb(double value) {
    return Math.log(getProb(value));
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#sampleVal()
   */
  public Object sampleVal() {
    checkHasParams();
    // rely on the fact that Util.random() returns a value in [0, 1)
    double x = lower + (Util.random() * (upper - lower));
    return x;
  }

  /**
   * Returns the parameter <code>lower</code>.
   */
  public double getLower() {
    return lower;
  }

  /**
   * Returns the parameter <code>upper</code>.
   */
  public double getUpper() {
    return upper;
  }

  @Override
  public String toString() {
    return getClass().getName();
  }

  private boolean hasLower;
  private boolean hasUpper;

  private double lower;
  private double upper;
}
