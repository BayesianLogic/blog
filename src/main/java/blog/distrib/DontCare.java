package blog.distrib;

/**
 * "Don't care" prior.
 * 
 * Use this for random variables which are always observed.
 * 
 * @author cberzan
 * @since November 25, 2014
 */
public class DontCare implements CondProbDistrib {

  /**
   * set parameters for DontCare distribution
   * 
   * @param params
   *          DontCare takes no parameters.
   * 
   * @see blog.distrib.CondProbDistrib#setParams(java.lang.Object[])
   */
  public void setParams(Object[] params) {
    assert (params.length == 0);
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#getProb(java.lang.Object)
   */
  public double getProb(Object value) {
    return 1.0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#getLogProb(java.lang.Object)
   */
  public double getLogProb(Object value) {
    return 0.0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#sampleVal()
   */
  public Object sampleVal() {
    throw new IllegalStateException(
        "Tried to sample from a DontCare distribution."
            + " Did you forget to provide an observation?");
  }

  @Override
  public String toString() {
    return getClass().getName();
  }

  @Override
  public Object[] getFiniteSupport() {
    return null;
  }

}
