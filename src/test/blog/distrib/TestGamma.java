/**
 * 
 */
package test.blog.distrib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import blog.common.Util;
import blog.distrib.Gamma;

/**
 * Unit Tests for Exponential Distribution
 */
public class TestGamma implements TestDistributions {
  private final double ERROR = 10e-5;

  void testDistributionRun(Gamma dist) {
    dist.getProb(-1);
    dist.getProb(0);
    dist.getProb(10);
    dist.getProb(1000);
  }

  /** Gamma with k = 2, Lambda = 1.0. */
  public void testGamma1(Gamma gamma) {
    assertEquals(2, gamma.getK(), ERROR);
    assertEquals(1.0, gamma.getLambda(), ERROR);

    assertEquals(0.0, gamma.getProb(-1), ERROR);
    assertEquals(0.0, gamma.getProb(0), ERROR);
    assertEquals(0.36787944117144233, gamma.getProb(1), ERROR);
    assertEquals(0.2706705664732255, gamma.getProb(2), ERROR);
    assertEquals(0.03368973499542735, gamma.getProb(5), ERROR);

    assertEquals(Double.NEGATIVE_INFINITY, gamma.getLogProb(-1), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY, gamma.getLogProb(0), ERROR);
    assertEquals(-1.0, gamma.getLogProb(1), ERROR);
    assertEquals(Math.log(0.2706705664732255), gamma.getLogProb(2), ERROR);
    assertEquals(Math.log(0.03368973499542735), gamma.getLogProb(5), ERROR);
  }

  /** Gamma with k = 5, Lambda = 0.5. */
  public void testGamma2(Gamma gamma) {
    assertEquals(5, gamma.getK(), ERROR);
    assertEquals(0.5, gamma.getLambda(), ERROR);

    assertEquals(0.0, gamma.getProb(0), ERROR);
    assertEquals(0.0, gamma.getProb(0), ERROR);
    assertEquals(0.066800942890542642, gamma.getProb(5), ERROR);
    assertEquals(0.0094583187005176771, gamma.getProb(20), ERROR);
    assertEquals(6.8705120747951929e-06, gamma.getProb(40), ERROR);

    assertEquals(Double.NEGATIVE_INFINITY, gamma.getLogProb(0), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY, gamma.getLogProb(0), ERROR);
    assertEquals(Math.log(0.066800942890542642), gamma.getLogProb(5), ERROR);
    assertEquals(Math.log(0.0094583187005176771), gamma.getLogProb(20), ERROR);
    assertEquals(Math.log(6.8705120747951929e-06), gamma.getLogProb(40), ERROR);
  }

  @Test
  public void testProbabilityViaConstructor() {
    Gamma gamma = new Gamma(2.0, 1.0);
    testGamma1(gamma);
    gamma = new Gamma(5.0, 0.5);
    testGamma2(gamma);
  }

  @Test
  public void testProbabilityViaSetParams() {
    Gamma gamma = new Gamma();
    gamma.setParams(Util.array(2.0, 1.0));
    testGamma1(gamma);
    gamma.setParams(Util.array(5.0, 0.5));
    testGamma2(gamma);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments() {
    Gamma gamma = new Gamma();
    gamma.setParams(Util.array(null));
    testDistributionRun(gamma);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments2() {
    Gamma gamma = new Gamma();
    gamma.setParams(Util.array(1.0, null));
    testDistributionRun(gamma);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments3() {
    Gamma gamma = new Gamma();
    gamma.setParams(Util.array(null, 2.1));
    testDistributionRun(gamma);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExtraneousArguments() {
    Gamma gamma = new Gamma();
    gamma.setParams(Util.array(1.0, 2.0, 1.2));
    testDistributionRun(gamma);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments() {
    Gamma gamma = new Gamma();
    gamma.setParams(Util.array(0.0, 1.0));
    testDistributionRun(gamma);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments2() {
    Gamma gamma = new Gamma();
    gamma.setParams(Util.array(1.5, -1.0));
    testDistributionRun(gamma);
  }

  @Test
  public void testDoubleSet() {
    Gamma gamma = new Gamma();
    gamma.setParams(Util.array(2.0, null));
    gamma.setParams(Util.array(null, 1.0));
    testGamma1(gamma);
    gamma.setParams(Util.array(5.0, null));
    gamma.setParams(Util.array(null, null));
    gamma.setParams(Util.array(null, 0.6));
    gamma.setParams(Util.array(null, 0.5));
    testGamma2(gamma);
  }

}
