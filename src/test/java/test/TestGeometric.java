/**
 * 
 */
package test;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import blog.distrib.Geometric;

/**
 * Unit tests for Geometric Distribution
 * src.blog.distrib.Geometric.java
 */
@RunWith(JUnit4.class)
public class TestGeometric {
  private final double ERROR_BOUND = 1e-6;

  @Test
  public void testCorrect() {
    testGeometric(0.1, 0, 0.1);
    testGeometric(0.1, 1, 0.09);
    testGeometric(0.1, 2, 0.081);
    testGeometric(0.6, 10, 6.291456e-5);
    testGeometric(0.05, 20, 0.017924296);
    testGeometric(0.1, -1, 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMissingArguments() {
    List<Number> params = new LinkedList<Number>();
    Geometric distribution = new Geometric(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNumberNotGivenAsProb() {
    List<Object> params = new LinkedList<Object>();
    params.add("foo");
    Geometric distribution = new Geometric(params);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testProbabilityNotGiven() {
    testGeometric(10, 0, 0.5);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMoreThanOneArgument() {
    List<Number> params = new LinkedList<Number>();
    params.add(0.5);
    params.add(0.6);
    Geometric distribution = new Geometric(params);
  }

  /**
   * For a Geometric Distribution with prob success = p
   * Asserts P(#failures until first success = k) = prob
   * No precondition on p, k, or prob
   */
  public void testGeometric(Number p, Integer k, Number prob) {
    List<Number> params = new LinkedList<Number>();
    params.add(p);
    Geometric distribution = new Geometric(params);
    List<Number> emptyArgs = new LinkedList<Number>();
    assertEquals(prob.doubleValue(), distribution.getProb(emptyArgs, k),
        ERROR_BOUND);
    assertEquals(Math.log(prob.doubleValue()),
        distribution.getLogProb(emptyArgs, k), ERROR_BOUND);

  }

}
