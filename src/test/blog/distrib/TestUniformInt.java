/**
 * 
 */
package test.blog.distrib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import blog.distrib.UniformInt;

/**
 * Unit Tests for UniformInt
 */
public class TestUniformInt implements TestDistributions {
  private final double ERROR = 10e-3;

  private void testDistributionRun(UniformInt unif) {
    unif.getProb(1);
    unif.getLogProb(1);
    unif.sampleVal();
  }

  /** UniformInt, lower = 0, upper = 0. */
  public void testUniformInt1(UniformInt unif) {
    assertEquals(0, unif.getLower(), ERROR);
    assertEquals(0, unif.getUpper(), ERROR);

    assertEquals(0, unif.getProb(-1), ERROR);
    assertEquals(1, unif.getProb(0), ERROR);
    assertEquals(0, unif.getProb(1), ERROR);
    assertEquals(0, unif.getProb(100), ERROR);

    assertEquals(Double.NEGATIVE_INFINITY, unif.getLogProb(-1), ERROR);
    assertEquals(0, unif.getLogProb(0), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY, unif.getLogProb(1), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY, unif.getLogProb(100), ERROR);
  }

  /** UniformInt, lower = 5, upper = 9. */
  public void testUniformInt2(UniformInt unif) {
    assertEquals(5, unif.getLower(), ERROR);
    assertEquals(9, unif.getUpper(), ERROR);

    assertEquals(0, unif.getProb(0), ERROR);
    assertEquals(0, unif.getProb(4), ERROR);
    assertEquals(0.2, unif.getProb(5), ERROR);
    assertEquals(0.2, unif.getProb(7), ERROR);
    assertEquals(0.2, unif.getProb(9), ERROR);
    assertEquals(0, unif.getProb(10), ERROR);

    assertEquals(Double.NEGATIVE_INFINITY, unif.getLogProb(0), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY, unif.getLogProb(4), ERROR);
    assertEquals(Math.log(0.2), unif.getLogProb(5), ERROR);
    assertEquals(Math.log(0.2), unif.getLogProb(7), ERROR);
    assertEquals(Math.log(0.2), unif.getLogProb(9), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY, unif.getLogProb(10), ERROR);
  }

  @Test
  public void testProbabilityViaConstructor() {
    // no longer needed. will be removed.
  }

  @Test
  public void testProbabilityViaSetParams() {
    UniformInt unif = new UniformInt();
    unif.setParams(new Object[] { 0, 0 });
    testUniformInt1(unif);
    unif.setParams(new Object[] { 5, 9 });
    testUniformInt2(unif);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments() {
    UniformInt unif = new UniformInt();
    unif.setParams(new Object[] { 2 });
    testDistributionRun(unif);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments2() {
    UniformInt unif = new UniformInt();
    unif.setParams(new Object[] { 2, null });
    testDistributionRun(unif);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments3() {
    UniformInt unif = new UniformInt();
    unif.setParams(new Object[] { null, 3 });
    testDistributionRun(unif);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments() {
    UniformInt unif = new UniformInt();
    unif.setParams(new Object[] { 4, 3 });
    testDistributionRun(unif);
  }

  @Test
  public void testDoubleSet() {
    UniformInt unif = new UniformInt();
    unif.setParams(new Object[] { null, 9 });
    unif.setParams(new Object[] { 5, null });
    testUniformInt2(unif);
    unif.setParams(new Object[] { null, null });
    testUniformInt2(unif);
  }

}
