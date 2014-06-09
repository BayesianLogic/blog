/**
 * 
 */
package test.blog.distrib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import blog.common.Util;
import blog.distrib.Bernoulli;

/**
 * Unit Tests for Bernoulli Distribution
 */
public class TestBernoulli implements TestDistributions {
  private final double ERROR = 10e-5;

  @Test
  public void testProbabilityViaConstructor() {
    // no longer testing.
  }

  @Test
  public void testProbabilityViaSetParams() {
    Bernoulli b = new Bernoulli();
    b.setParams(Util.array(0.4));
    assertEquals(0.4, b.getP(), ERROR);
    testDistribution(b);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments() {
    Bernoulli b = new Bernoulli();
    b.setParams(Util.array(null));
    testDistribution(b);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExtraneousArgs() {
    Bernoulli b = new Bernoulli();
    b.setParams(Util.array(1.0, 0.5));
    testDistribution(b);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments() {
    Bernoulli b = new Bernoulli();
    b.setParams(Util.array(1.1));
    testDistribution(b);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments2() {
    Bernoulli b = new Bernoulli();
    b.setParams(Util.array(-0.1));
    testDistribution(b);
  }

  @Test
  public void testDoubleSet() {
    Bernoulli b = new Bernoulli();
    b.setParams(Util.array(null));
    b.setParams(Util.array(0.5));
    assertEquals(0.5, b.getP(), ERROR);
    b.setParams(Util.array(0.7));
    assertEquals(0.7, b.getP(), ERROR);
    testDistribution(b);
  }

  public void testDistribution(Bernoulli bernoulli) {
    double p = bernoulli.getP();
    assertEquals(p, bernoulli.getProb(1), ERROR);
    assertEquals(Math.log(p), bernoulli.getLogProb(1), ERROR);
    assertEquals(1 - p, bernoulli.getProb(0), ERROR);
    assertEquals(Math.log(1 - p), bernoulli.getLogProb(0), ERROR);
  }

}
