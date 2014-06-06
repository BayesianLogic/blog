/**
 * 
 */
package test.blog.distrib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import blog.common.Util;
import blog.distrib.Beta;

/**
 * @author cgioia
 * @since May 27, 2014
 */
public class TestBeta implements TestDistributions {
  private final double ERROR = 10e-5;

  /** Tests that getProb and getLogProb run without error. */
  public void testBetaRun(Beta beta) {
    beta.getProb(0);
    beta.getProb(0.5);
    beta.getProb(1.0);
    beta.getLogProb(0);
    beta.getLogProb(0.5);
    beta.getLogProb(1);
  }

  /** Beta(1, 1) Distribution. **/
  public void testBeta1_1(Beta beta) {
    assertEquals(1.0, beta.getA(), ERROR);
    assertEquals(1.0, beta.getB(), ERROR);

    assertEquals(0.0, beta.getProb(-0.01), ERROR);
    assertEquals(1.0, beta.getProb(0.0), ERROR);
    assertEquals(1.0, beta.getProb(0.5), ERROR);
    assertEquals(1.0, beta.getProb(1.0), ERROR);
    assertEquals(0.0, beta.getProb(1.5), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY, beta.getLogProb(-0.01), ERROR);
    assertEquals(0, beta.getLogProb(0.0), ERROR);
    assertEquals(0, beta.getLogProb(0.5), ERROR);
    assertEquals(0, beta.getLogProb(1.0), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY, beta.getLogProb(1.5), ERROR);
  }

  /** Beta(1, 2) Distribution. **/
  public void testBeta1_2(Beta beta) {
    assertEquals(1.0, beta.getA(), ERROR);
    assertEquals(2.0, beta.getB(), ERROR);

    assertEquals(0.0, beta.getProb(-0.01), ERROR);
    assertEquals(2.0, beta.getProb(0), ERROR);
    assertEquals(1.0, beta.getProb(0.5), ERROR);
    assertEquals(0.8, beta.getProb(0.6), ERROR);
    assertEquals(0.0, beta.getProb(1), ERROR);
    assertEquals(0.0, beta.getProb(1.01), ERROR);

    assertEquals(Math.log(2.0), beta.getLogProb(0), ERROR);
    assertEquals(0, beta.getLogProb(0.5), ERROR);
    assertEquals(Math.log(0.8), beta.getLogProb(0.6), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY, beta.getLogProb(1), ERROR);
  }

  /** Beta(2, 2) Distribution. **/
  public void testBeta2_2(Beta beta) {
    assertEquals(2.0, beta.getA(), ERROR);
    assertEquals(2.0, beta.getB(), ERROR);

    assertEquals(0.0, beta.getProb(0), ERROR);
    assertEquals(1.5, beta.getProb(0.5), ERROR);
    assertEquals(0.0, beta.getProb(1.0), ERROR);

    assertEquals(Double.NEGATIVE_INFINITY, beta.getLogProb(0), ERROR);
    assertEquals(Math.log(1.5), beta.getLogProb(0.5), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY, beta.getLogProb(1), ERROR);
  }

  /** Beta(0.5, 5.8) Distribution. **/
  public void testBeta(Beta beta) {
    assertEquals(0.5, beta.getA(), ERROR);
    assertEquals(5.8, beta.getB(), ERROR);

    assertEquals(Double.POSITIVE_INFINITY, beta.getProb(0.0), ERROR);
    assertEquals(2.5360324296863515, beta.getProb(0.1), ERROR);
    assertEquals(0.067509015738754771, beta.getProb(0.5), ERROR);
    assertEquals(0.0, beta.getProb(1.0), ERROR);

    assertEquals(Double.POSITIVE_INFINITY, beta.getLogProb(0.0), ERROR);
    assertEquals(0.93060082422442825, beta.getLogProb(0.1), ERROR);
    assertEquals(-2.6954941235227929, beta.getLogProb(0.5), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY, beta.getLogProb(1.0), ERROR);
  }

  @Test
  public void testProbabilityViaConstructor() {
    Beta beta = new Beta(1, 1);
    testBeta1_1(beta);
    beta = new Beta(1.0, 2.0);
    testBeta1_2(beta);
    beta = new Beta(2.0, 2.0);
    testBeta2_2(beta);
    beta = new Beta(0.5, 5.8);
    testBeta(beta);
  }

  @Test
  public void testProbabilityViaSetParams() {
    Beta beta = new Beta();
    beta.setParams(Util.array(1.0, 1.0));
    testBeta1_1(beta);
    beta.setParams(Util.array(1.0, 2.0));
    testBeta1_2(beta);
    beta.setParams(Util.array(2.0, 2.0));
    testBeta2_2(beta);
    beta.setParams(Util.array(0.5, 5.8));
    testBeta(beta);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments() {
    Beta beta = new Beta();
    beta.setParams(Util.array(null, 1.0));
    testBetaRun(beta);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments2() {
    Beta beta = new Beta();
    beta.setParams(Util.array(1.0, null));
    testBetaRun(beta);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments3() {
    Beta beta = new Beta();
    beta.setParams(Util.array(2.0));
    testBetaRun(beta);
  }

  @Test(expected = IllegalArgumentException.class)
  public void extraneousArguments() {
    Beta beta = new Beta();
    beta.setParams(Util.array(1.0, 2.0, 3.0));
    testBetaRun(beta);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments() {
    Beta beta = new Beta();
    beta.setParams(Util.array(0.0, 2.0));
    testBetaRun(beta);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments2() {
    Beta beta = new Beta();
    beta.setParams(Util.array(2.0, 0.0));
    testBetaRun(beta);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments3() {
    Beta beta = new Beta();
    beta.setParams(Util.array(-1.0, -1.0));
    testBetaRun(beta);
  }

  @Test
  public void testDoubleSet() {
    Beta beta = new Beta();
    beta.setParams(Util.array(1.0, null));
    beta.setParams(Util.array(null, 1.0));
    testBeta1_1(beta);
    beta.setParams(Util.array(null, 2.0));
    testBeta1_2(beta);
    beta.setParams(Util.array(null, null));
    beta.setParams(Util.array(0.5, null));
    beta.setParams(Util.array(null, 5.8));
    testBeta(beta);
  }

}
