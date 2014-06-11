/**
 * 
 */
package test.blog.distrib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import blog.distrib.Geometric;

/**
 * Unit tests for Geometric Distribution
 * src.blog.distrib.Geometric.java
 */
@RunWith(JUnit4.class)
public class TestGeometric implements TestDistributions {
  private final double ERROR = 1e-6;

  public void testDistributionRun(Geometric geom) {
    geom.getProb(-1);
    geom.getProb(0);
    geom.getProb(1);
    geom.getProb(1000);
  }

  /** Geometric with alpha = 1. */
  public void testGeometric1(Geometric geom) {
    assertEquals(1, geom.getAlpha(), ERROR);

    assertEquals(0, geom.getProb(-1), ERROR);
    assertEquals(1, geom.getProb(0), ERROR);
    assertEquals(0, geom.getProb(1), ERROR);
    assertEquals(0, geom.getProb(20), ERROR);

    assertEquals(Double.NEGATIVE_INFINITY, geom.getLogProb(-1), ERROR);
    assertEquals(0, geom.getLogProb(0), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY, geom.getLogProb(1), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY, geom.getLogProb(20), ERROR);
  }

  /** Geometric with alpha = 0.2. */
  public void testGeometric2(Geometric geom) {
    assertEquals(0.2, geom.getAlpha(), ERROR);

    assertEquals(0, geom.getProb(-1), ERROR);
    assertEquals(0.2, geom.getProb(0), ERROR);
    assertEquals(0.16, geom.getProb(1), ERROR);
    assertEquals(0.128, geom.getProb(2), ERROR);
    assertEquals(0.021474836480000013, geom.getProb(10), ERROR);
    assertEquals(2.6584559915698375e-05, geom.getProb(40), ERROR);

    assertEquals(Double.NEGATIVE_INFINITY, geom.getLogProb(-1), ERROR);
    assertEquals(Math.log(0.2), geom.getLogProb(0), ERROR);
    assertEquals(Math.log(0.16), geom.getLogProb(1), ERROR);
    assertEquals(Math.log(0.128), geom.getLogProb(2), ERROR);
    assertEquals(Math.log(0.021474836480000013), geom.getLogProb(10), ERROR);
    assertEquals(Math.log(2.6584559915698375e-05), geom.getLogProb(40), ERROR);
  }

  @Test
  public void testProbabilityViaConstructor() {
    Geometric geom = new Geometric(1);
    testGeometric1(geom);
    geom = new Geometric(0.2);
    testGeometric2(geom);
  }

  @Test
  public void testProbabilityViaSetParams() {
    Geometric geom = new Geometric();
    geom.setParams(new Object[] { 1.0 });
    testGeometric1(geom);
    geom.setParams(new Object[] { 0.2 });
    testGeometric2(geom);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments() {
    Geometric geom = new Geometric();
    geom.setParams(new Object[] { null });
    testDistributionRun(geom);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExtraneousArguments() {
    Geometric geom = new Geometric();
    geom.setParams(new Object[] { 1.0, 2.0 });
    testDistributionRun(geom);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments() {
    Geometric geom = new Geometric();
    geom.setParams(new Object[] { 0.0 });
    testDistributionRun(geom);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments2() {
    Geometric geom = new Geometric();
    geom.setParams(new Object[] { 1.01 });
    testDistributionRun(geom);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments3() {
    Geometric geom = new Geometric();
    geom.setParams(new Object[] { -1.0 });
    testDistributionRun(geom);
  }

  @Test
  public void testDoubleSet() {
    Geometric geom = new Geometric();
    geom.setParams(new Object[] { 1.0 });
    geom.setParams(new Object[] { null });
    testGeometric1(geom);
    geom.setParams(new Object[] { null });
    testGeometric1(geom);
  }

}
