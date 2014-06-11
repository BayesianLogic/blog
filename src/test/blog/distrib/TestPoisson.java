/**
 * 
 */
package test.blog.distrib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import blog.distrib.Poisson;

/**
 * Unit Tests for Poisson Distribution
 */
public class TestPoisson implements TestDistributions {
  private final double ERROR = 10e-5;

  public void testDistributionRun(Poisson poiss) {
    poiss.getProb(-1);
    poiss.getProb(0);
    poiss.getProb(1);
    poiss.getProb(1000);
    poiss.getLogProb(-1);
  }

  /** Poisson with lambda = 1.0. */
  public void testPoisson1(Poisson poiss) {
    assertEquals(1.0, poiss.getLambda(), ERROR);

    assertEquals(0.36787944117144233, poiss.getProb(0), ERROR);
    assertEquals(0.36787944117144233, poiss.getProb(1), ERROR);
    assertEquals(0.18393972058572114, poiss.getProb(2), ERROR);
    assertEquals(0.00306566200976202, poiss.getProb(5), ERROR);

    assertEquals(-1.0, poiss.getLogProb(0), ERROR);
    assertEquals(-1.0, poiss.getLogProb(1), ERROR);
    assertEquals(-1 - Math.log(2), poiss.getLogProb(2), ERROR);
    assertEquals(-5.787491742782046, poiss.getLogProb(5), ERROR);
  }

  /** Poisson with lambda = 5.0. */
  public void testPoisson2(Poisson poiss) {
    assertEquals(5.0, poiss.getLambda(), ERROR);

    assertEquals(0.006737946999085467, poiss.getProb(0), ERROR);
    assertEquals(0.17546736976785068, poiss.getProb(5), ERROR);
    assertEquals(0.018132788707821854, poiss.getProb(10), ERROR);
    assertEquals(2.6412107749256406e-07, poiss.getProb(20), ERROR);

    assertEquals(-5.0, poiss.getLogProb(0), ERROR);
    assertEquals(-1.7403021806115442, poiss.getLogProb(5), ERROR);
    assertEquals(-4.010033448734513, poiss.getLogProb(10), ERROR);
    assertEquals(-15.146858212071479, poiss.getLogProb(20), ERROR);
  }

  @Test
  public void testProbabilityViaConstructor() {
    // no longer needed. will be removed.
  }

  @Test
  public void testProbabilityViaSetParams() {
    Poisson poiss = new Poisson();
    poiss.setParams(new Object[] { 1.0 });
    testPoisson1(poiss);
    poiss.setParams(new Object[] { 5.0 });
    testPoisson2(poiss);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments() {
    Poisson poiss = new Poisson();
    poiss.setParams(new Object[] { null });
    testDistributionRun(poiss);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExtraneousArguments() {
    Poisson poiss = new Poisson();
    poiss.setParams(new Object[] { 1.0, 5.1 });
    testDistributionRun(poiss);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments() {
    Poisson poiss = new Poisson();
    poiss.setParams(new Object[] { -0.5 });
    testDistributionRun(poiss);
  }

  @Test
  public void testDoubleSet() {
    Poisson poiss = new Poisson();
    poiss.setParams(new Object[] { null });
    poiss.setParams(new Object[] { 1.0 });
    testPoisson1(poiss);
    poiss.setParams(new Object[] { 5.0 });
    poiss.setParams(new Object[] { null });
    testPoisson2(poiss);
  }

}
