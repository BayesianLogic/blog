/**
 * 
 */
package test.blog.distrib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import blog.distrib.UniformReal;

/**
 * Unit Tests for Uniform Real
 */
public class TestUniformReal implements TestDistributions {
  private final double ERROR = 1e-10;
  private final double EPSILON = 1e-2;

  /** Uniform Distribution, lower = 1.0, upper = 3.0 . */
  public void testUniform1(UniformReal unif) {
    assertEquals(0.0, unif.getProb(0.99), ERROR);
    assertEquals(0.5, unif.getProb(1.0), ERROR);
    assertEquals(0.5, unif.getProb(1.5), ERROR);
    assertEquals(0.5, unif.getProb(2.5), ERROR);
    assertEquals(0.5, unif.getProb(2.99), ERROR);
    assertEquals(0.0, unif.getProb(3.000), ERROR);
    assertEquals(0.0, unif.getProb(3.1), ERROR);
    assertEquals(0.0, unif.getProb(20), ERROR);

    assertEquals(Double.NEGATIVE_INFINITY, unif.getLogProb(0.99), ERROR);
    assertEquals(Math.log(0.5), unif.getLogProb(1.0), ERROR);
    assertEquals(Math.log(0.5), unif.getLogProb(1.5), ERROR);
    assertEquals(Math.log(0.5), unif.getLogProb(2.5), ERROR);
    assertEquals(Math.log(0.5), unif.getLogProb(2.99), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY, unif.getLogProb(3.000), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY, unif.getLogProb(3.1), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY, unif.getLogProb(20), ERROR);
  }

  @Test
  public void testProbabilityViaConstructor() {
    // no longer needed. will be removed.
  }

  @Test
  public void testProbabilityViaSetParams() {
    UniformReal unif = new UniformReal();
    unif.setParams(new Object[] { 1.0, 3.0 });
    testUniform1(unif);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments() {
    UniformReal unif = new UniformReal();
    unif.setParams(new Object[] { 1.0, null });
    unif.sampleVal();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments() {
    UniformReal unif = new UniformReal();
    unif.setParams(new Object[] { 2.0, 1.9 });
    unif.sampleVal();
  }

  @Test
  public void testDoubleSet() {
    UniformReal unif = new UniformReal();
    unif.setParams(new Object[] { null, 3.0 });
    unif.setParams(new Object[] { 1.0, null });
    testUniform1(unif);
  }
}
