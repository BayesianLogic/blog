package blog.distrib;

import java.util.List;

import blog.common.Util;

/**
 * Uniform distribution over a range of real numbers [<code>lower</code>,
 * <code>upper</code>).
 * 
 * @author cgioia
 * @since June 17, 2014
 */
public class UniformReal implements CondProbDistrib {

  /**
   * set parameters for UniformReal distribution
   * 
   * @param params
   *          An array of the form [Number, Number]
   *          <ul>
   *          <li>params[0]:<code>lower</code>(Number)</li>
   *          <li>params[1]:<code>upper</code>(Number)</li>
   *          </ul>
   * 
   * @see blog.distrib.CondProbDistrib#setParams(java.lang.Object[])
   */
  public void setParams(Object[] params) {
    if (params.length != 2) {
      throw new IllegalArgumentException("expected two params: lower and upper");
    }
    setParams((Number) params[0], (Number) params[1]);
  }

  /**
   * If the method parameter lower is non-null, sets the distribution parameter
   * <code>lower</code> to lower. Similarly for upper. Once both parameters
   * <code>lower</code> and <code>upper</code> have been set, checks to see that
   * <code>lower</code> < <code>upper</code>.
   */
  public void setParams(Number lower, Number upper) {
    if (lower != null) {
      this.lower = lower.doubleValue();
      this.hasLower = true;
    }
    if (upper != null) {
      this.upper = upper.doubleValue();
      this.hasUpper = true;
    }
    if (this.hasLower && this.hasUpper) {
      if (this.lower >= this.upper) {
        throw new IllegalArgumentException("lower >= upper");
      }
      this.density = 1 / (this.upper - this.lower);
      this.logDensity = Math.log(this.density);
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
    return getProb(((Number) value).doubleValue());
  }

  /**
   * Returns the probability of <code>value</code>.
   */
  public double getProb(double value) {
    checkHasParams();
    return (value >= lower) && (value < upper) ? this.density : 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#getLogProb(java.lang.Object)
   */
  public double getLogProb(Object value) {
    return getLogProb(((Number) value).doubleValue());
  }

  /**
   * Returns the log probability of <code>value</code>.
   */
  public double getLogProb(double value) {
    checkHasParams();
    return (value >= lower) && (value < upper) ? this.logDensity
        : Double.NEGATIVE_INFINITY;
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#sampleVal()
   */
  public Object sampleVal() {
    return sample_value();
  }

  /** Samples uniformly between <code>lower</code> and <code>upper</code>. */
  public double sample_value() {
    checkHasParams();
    // rely on the fact that Util.random() returns a value in [0, 1)
    return (lower + (Util.random() * (upper - lower)));
  }

  @Override
  public String toString() {
    return getClass().getName();
  }

  @Override
  public List<Object> getFiniteSupport() {
    checkHasParams();
    return null;
  }

  private boolean hasLower;
  private boolean hasUpper;

  private double lower;
  private double upper;
  private double density;
  private double logDensity;
}
