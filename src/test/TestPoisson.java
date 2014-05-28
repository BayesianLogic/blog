/**
 * 
 */
package test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import blog.distrib.Poisson;

/**
 * Unit Tests for Poisson Distribution
 */
public class TestPoisson extends TestDistribution {

  @Test
  public void testGetProb() {
    constructParams.add(1.0);
    Poisson p = new Poisson(constructParams);
    assertEquals(0.36787944117144233, p.getProb(args, 0), ERROR_BOUND);
    assertEquals(0.36787944117144233, p.getProb(args, 1), ERROR_BOUND);
    assertEquals(0.18393972058572114, p.getProb(args, 2), ERROR_BOUND);
    assertEquals(0.00306566200976202, p.getProb(args, 5), ERROR_BOUND);

    constructParams.clear();
    constructParams.add(5.0);
    p = new Poisson(constructParams);
    assertEquals(0.006737946999085467, p.getProb(args, 0), ERROR_BOUND);
    assertEquals(0.17546736976785068, p.getProb(args, 5), ERROR_BOUND);
    assertEquals(0.018132788707821854, p.getProb(args, 10), ERROR_BOUND);
    assertEquals(2.6412107749256406e-07, p.getProb(args, 20), ERROR_BOUND);
  }

  @Test
  public void testGetLogProb() {
    constructParams.add(1.0);
    Poisson p = new Poisson(constructParams);
    assertEquals(-1.0, p.getLogProb(args, 0), ERROR_BOUND);
    assertEquals(-1.0, p.getLogProb(args, 1), ERROR_BOUND);
    assertEquals(-1 - Math.log(2), p.getLogProb(args, 2), ERROR_BOUND);
    assertEquals(-5.787491742782046, p.getLogProb(args, 5), ERROR_BOUND);

    constructParams.clear();
    constructParams.add(5.0);
    p = new Poisson(constructParams);
    assertEquals(-5.0, p.getLogProb(args, 0), ERROR_BOUND);
    assertEquals(-1.7403021806115442, p.getLogProb(args, 5), ERROR_BOUND);
    assertEquals(-4.010033448734513, p.getLogProb(args, 10), ERROR_BOUND);
    assertEquals(-15.146858212071479, p.getLogProb(args, 20), ERROR_BOUND);
  }

}
