/**
 * 
 */
package test.blog.distrib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

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
    b.setParams(new Object[] { 0.4 });
    assertEquals(0.4, b.getProb(1), ERROR);
    assertEquals(0.6, b.getProb(0), ERROR);
    assertEquals(0.0, b.getProb(2), ERROR);
    assertEquals(Math.log(0.4), b.getLogProb(1), ERROR);
    assertEquals(Math.log(0.6), b.getLogProb(0), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY, b.getLogProb(2), ERROR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments() {
    Bernoulli b = new Bernoulli();
    b.setParams(new Object[] { null });
    b.sampleVal();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExtraneousArgs() {
    Bernoulli b = new Bernoulli();
    b.setParams(new Object[] { 1.0, 0.5 });
    b.sampleVal();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments() {
    Bernoulli b = new Bernoulli();
    b.setParams(new Object[] { 1.1 });
    b.sampleVal();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments2() {
    Bernoulli b = new Bernoulli();
    b.setParams(new Object[] { -0.1 });
    b.sampleVal();
  }

  @Test
  public void testDoubleSet() {
    Bernoulli b = new Bernoulli();
    b.setParams(new Object[] { null });
    b.setParams(new Object[] { 0.5 });
    assertEquals(0.5, b.getProb(1), ERROR);
    assertEquals(0.5, b.getProb(0), ERROR);
    b.setParams(new Object[] { 0.7 });
    assertEquals(0.7, b.getProb(1), ERROR);
    assertEquals(0.3, b.getProb(0), ERROR);
  }

}
