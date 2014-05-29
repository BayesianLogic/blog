package blog.distrib;

import java.util.List;

/**
 * Common interface for all probability distributions.
 * 
 * To implement a new probability distribution, write a class that implements
 * this interface. Provide a constructor that takes no parameters. For full
 * examples, see UnivarGaussian or UniformReal.
 */
public interface CondProbDistrib {

  /**
   * Set the parameters of this distribution.
   * 
   * If a parameter is set to null, that means the old value should be used.
   * 
   * This method can be called more than once. For example:
   * 
   * CondProbDistrib cpd = new UnivarGaussian();
   * // Now, mu and sigma are not set.
   * cpd.setParams(Util.list(1.0, null));
   * // Now, mu is 1.0 and sigma is not set.
   * cpd.setParams(Util.list(2.0, 4.0));
   * // Now, mu is 2.0 and sigma is 4.0.
   * cpd.setParams(Util.list(null, 5.0));
   * // Now, mu is still 2.0 and sigma is 5.0.
   * 
   * @param params
   *          The list of parameters for this distribution.
   */
  public void setParams(List<Object> params);

  /**
   * Compute the probability of a given value.
   * 
   * @param value
   *          The value whose probability to compute.
   * @return The probability of the given value.
   */
  public double getProb(Object value);

  /**
   * Compute the log-probability of a given value.
   * 
   * @param value
   *          The value whose log-probability to compute.
   * @return The log-probability of the given value.
   */
  public double getLogProb(Object value);

  /**
   * Sample a value from this distribution.
   * 
   * @return The sample value.
   */
  public Object sampleVal();
}
