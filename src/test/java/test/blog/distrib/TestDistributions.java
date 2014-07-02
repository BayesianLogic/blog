/**
 * 
 */
package test.blog.distrib;

/**
 * Tests the correctness of Distributions that implement the
 * blog.distrib.CondProb Interface
 * 
 * @author chrisgioia64
 * @since Jun 4, 2014
 * 
 */
public interface TestDistributions {

  /**
   * When the appropriate parameters for a distribution have been provided via
   * the constructor, tests the correctness of getProb and getLogProb methods
   */
  void testProbabilityViaConstructor();

  /**
   * When the appropriate parameters for a distribution have been provided via
   * the setParams method, tests the correctness of getProb and getLogProb
   * methods
   */
  void testProbabilityViaSetParams();

  /**
   * When insufficient parameters have been provided for a distribution,
   * tests that an exception is raised.
   */
  void testInsufficientArguments();

  /**
   * Invalid distribution parameters have been provided.
   * An exception should be raised.
   * (e.g. For the uniform real, upper < lower)
   */
  void testIncorrectArguments();

  /**
   * Test that calling setParams multiple times results in the correct behavior.
   */
  void testDoubleSet();

  /**
   * If the distribution takes a Double as one of its elements to setParams, a
   * user can choose to pass in an Integer, and in this case, there should be a
   * proper cast from the Integer value to its Double equivalent.
   */
  void testSetParamsIntegerArguments();

  /**
   * If the distribution takes a Double as argument to getProb and getLogProb, a
   * user should be able to pass in an Integer instead, and in this case, there
   * should be a proper cast from the Integer value its Double equivalent
   */
  void testGetProbIntegerArguments();
}
