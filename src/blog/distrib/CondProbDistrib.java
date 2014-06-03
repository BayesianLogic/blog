package blog.distrib;

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
   * <code>
   * CondProbDistrib cpd = new UnivarGaussian();
   * // Now, mu and sigma are not set.
   * cpd.setParams(Util.list(1.0, null));
   * // Now, mu is 1.0 and sigma is not set.
   * cpd.setParams(Util.list(2.0, 4.0));
   * // Now, mu is 2.0 and sigma is 4.0.
   * cpd.setParams(Util.list(null, 5.0));
   * // Now, mu is still 2.0 and sigma is 5.0.
   * </code>
   * 
   * For efficiency reasons, this method MAY keep a reference to a param,
   * rather than making a copy. So it is not safe to modify the params after
   * passing them to this method. For example:
   * 
   * <code>
   * Set someSet = ...;
   * CondProbDistrib cpd = new UniformChoice(Util.list(someSet));
   * someSet.add(...); // unsafe; don't do this
   * </code>
   * 
   * @param params
   *          The array of parameters for this distribution.
   */
  public void setParams(Object[] params);

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
