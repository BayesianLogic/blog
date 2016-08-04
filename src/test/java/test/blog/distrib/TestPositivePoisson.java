/**
 * 
 */
package test.blog.distrib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import blog.distrib.PositivePoisson;

public class TestPositivePoisson {

  private final double ERROR = 10e-5;

  public void testDistributionRun(PositivePoisson poiss) {
    poiss.getProb(-1);
    poiss.getProb(0);
    poiss.getProb(1);
    poiss.getProb(1000);
    poiss.getLogProb(-1);
  }

  /** Poisson with lambda = 2.0. */
  public void testPoisson1(PositivePoisson poiss) {
    assertEquals(0.36787944117144233, poiss.getProb(1), ERROR);
    assertEquals(0.36787944117144233, poiss.getProb(2), ERROR);
    assertEquals(0.18393972058572114, poiss.getProb(3), ERROR);
    assertEquals(0.00306566200976202, poiss.getProb(6), ERROR);

    assertEquals(-1.0, poiss.getLogProb(1), ERROR);
    assertEquals(-1.0, poiss.getLogProb(2), ERROR);
    assertEquals(-1 - Math.log(2), poiss.getLogProb(3), ERROR);
    assertEquals(-5.787491742782046, poiss.getLogProb(6), ERROR);
  }

  /** Poisson with lambda = 6.0. */
  public void testPoisson2(PositivePoisson poiss) {
    assertEquals(0.006737946999085467, poiss.getProb(1), ERROR);
    assertEquals(0.17546736976785068, poiss.getProb(6), ERROR);
    assertEquals(0.018132788707821854, poiss.getProb(11), ERROR);
    assertEquals(2.6412107749256406e-07, poiss.getProb(21), ERROR);

    assertEquals(-5.0, poiss.getLogProb(1), ERROR);
    assertEquals(-1.7403021806115442, poiss.getLogProb(6), ERROR);
    assertEquals(-4.010033448734513, poiss.getLogProb(11), ERROR);
    assertEquals(-15.146858212071479, poiss.getLogProb(21), ERROR);
  }

  @Test
  public void testProbabilityViaConstructor() {
    // no longer needed. will be removed.
  }

  @Test
  public void testProbabilityViaSetParams() {
    PositivePoisson poiss = new PositivePoisson();
    poiss.setParams(new Object[] { 2.0 });
    testPoisson1(poiss);
    poiss.setParams(new Object[] { 6.0 });
    testPoisson2(poiss);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments() {
    PositivePoisson poiss = new PositivePoisson();
    poiss.setParams(new Object[] { null });
    testDistributionRun(poiss);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExtraneousArguments() {
    PositivePoisson poiss = new PositivePoisson();
    poiss.setParams(new Object[] { 1.0, 5.1 });
    testDistributionRun(poiss);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments() {
    PositivePoisson poiss = new PositivePoisson();
    poiss.setParams(new Object[] { -0.5 });
    testDistributionRun(poiss);
  }

  @Test
  public void testDoubleSet() {
    PositivePoisson poiss = new PositivePoisson();
    poiss.setParams(new Object[] { null });
    poiss.setParams(new Object[] { 2.0 });
    testPoisson1(poiss);
    poiss.setParams(new Object[] { 6.0 });
    poiss.setParams(new Object[] { null });
    testPoisson2(poiss);
  }

  @Test
  public void testSetParamsIntegerArguments() {
    PositivePoisson poiss = new PositivePoisson();
    poiss.setParams(new Object[] { new Integer(2) });
    testPoisson1(poiss);
    poiss.setParams(new Object[] { new Integer(6) });
    testPoisson2(poiss);
  }
}
