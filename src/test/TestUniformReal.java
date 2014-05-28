/**
 * 
 */
package test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import blog.distrib.UniformReal;

/**
 * Unit Tests for Uniform Real
 */
public class TestUniformReal extends TestDistribution {

  @Test
  public void testGetProb() {
    constructParams.add(1.0);
    constructParams.add(3.0);
    UniformReal unif = new UniformReal(constructParams);
    assertEquals(0.0, unif.getProb(args, 0.99), ERROR_BOUND);
    assertEquals(0.5, unif.getProb(args, 1.0), ERROR_BOUND);
    assertEquals(0.5, unif.getProb(args, 2.0), ERROR_BOUND);
    assertEquals(0.5, unif.getProb(args, 2.5), ERROR_BOUND);
    assertEquals(0.5, unif.getProb(args, 2.99), ERROR_BOUND);
    assertEquals(0.0, unif.getProb(args, 3.0), ERROR_BOUND);
    assertEquals(0.0, unif.getProb(args, 3.01), ERROR_BOUND);
  }

  @Test
  public void testGetLogProb() {
    constructParams.add(1.0);
    constructParams.add(3.0);
    UniformReal unif = new UniformReal(constructParams);
    assertEquals(Double.NEGATIVE_INFINITY, unif.getLogProb(args, 0.99),
        ERROR_BOUND);
    assertEquals(Math.log(0.5), unif.getLogProb(args, 1.0), ERROR_BOUND);
    assertEquals(Math.log(0.5), unif.getLogProb(args, 2.0), ERROR_BOUND);
    assertEquals(Math.log(0.5), unif.getLogProb(args, 2.5), ERROR_BOUND);
    assertEquals(Math.log(0.5), unif.getLogProb(args, 2.9), ERROR_BOUND);
    assertEquals(Double.NEGATIVE_INFINITY, unif.getLogProb(args, 3.0),
        ERROR_BOUND);
    assertEquals(Double.NEGATIVE_INFINITY, unif.getLogProb(args, 3.01),
        ERROR_BOUND);
  }

}
