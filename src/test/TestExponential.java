/**
 * 
 */
package test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import blog.distrib.Exponential;

/**
 * Unit Tests for Exponential Distribution
 */
public class TestExponential extends TestDistribution {

  @Test
  public void testGetProb() {
    constructParams.add(1.0);
    Exponential exp = new Exponential(constructParams);
    assertEquals(1.0, exp.getProb(args, 0), ERROR_BOUND);
    assertEquals(0.36787944117144233, exp.getProb(args, 1), ERROR_BOUND);
    assertEquals(0.1353352832366127, exp.getProb(args, 2), ERROR_BOUND);
    assertEquals(0.006737946999085467, exp.getProb(args, 5), ERROR_BOUND);

    constructParams.clear();
    constructParams.add(25.0);
    exp = new Exponential(constructParams);
    assertEquals(25.0, exp.getProb(args, 0), ERROR_BOUND);
    assertEquals(7.1626199215047519, exp.getProb(args, 0.05), ERROR_BOUND);
    assertEquals(3.4719859662410053e-10, exp.getProb(args, 1), ERROR_BOUND);
  }

  @Test
  public void testGetLogProb() {
    constructParams.add(1.0);
    Exponential exp = new Exponential(constructParams);
    assertEquals(0.0, exp.getLogProb(args, 0), ERROR_BOUND);
    assertEquals(-1.0, exp.getLogProb(args, 1), ERROR_BOUND);
    assertEquals(-2.0, exp.getLogProb(args, 2), ERROR_BOUND);
    assertEquals(-5.0, exp.getLogProb(args, 5), ERROR_BOUND);

    constructParams.clear();
    constructParams.add(25.0);
    exp = new Exponential(constructParams);
    assertEquals(3.2188758248682006, exp.getLogProb(args, 0), ERROR_BOUND);
    assertEquals(1.9688758248682006, exp.getLogProb(args, 0.05), ERROR_BOUND);
    assertEquals(-21.7811241751318, exp.getLogProb(args, 1), ERROR_BOUND);
  }

}
