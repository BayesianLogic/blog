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
}
