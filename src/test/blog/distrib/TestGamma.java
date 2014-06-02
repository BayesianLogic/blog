/**
 * 
 */
package test.blog.distrib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import blog.distrib.Gamma;

/**
 * Unit Tests for Exponential Distribution
 */
public class TestGamma extends TestDistribution {

  @Test
  public void testGetProb() {
    constructParams.add(2);
    constructParams.add(1.0);
    Gamma gamma = new Gamma(constructParams);
    assertEquals(0.0, gamma.getProb(args, 0), ERROR_BOUND);
    assertEquals(0.36787944117144233, gamma.getProb(args, 1), ERROR_BOUND);
    assertEquals(0.2706705664732255, gamma.getProb(args, 2), ERROR_BOUND);
    assertEquals(0.03368973499542735, gamma.getProb(args, 5), ERROR_BOUND);

    constructParams.clear();
    constructParams.add(3);
    constructParams.add(20);
    gamma = new Gamma(constructParams);
    assertEquals(0.0, gamma.getProb(args, 0), ERROR_BOUND);
    assertEquals(3.678794411714427, gamma.getProb(args, 0.05), ERROR_BOUND);
    assertEquals(3.4719859662410053e-10, gamma.getProb(args, 1), ERROR_BOUND);
  }

  @Test
  public void testGetLogProb() {
    constructParams.add(2);
    constructParams.add(1.0);
    Gamma gamma = new Gamma(constructParams);
    assertEquals(Double.NEGATIVE_INFINITY, gamma.getLogProb(args, 0),
        ERROR_BOUND);
    assertEquals(-1.0, gamma.getLogProb(args, 1), ERROR_BOUND);
    assertEquals(Math.log(0.2706705664732255), gamma.getLogProb(args, 2),
        ERROR_BOUND);
    assertEquals(Math.log(0.03368973499542735), gamma.getLogProb(args, 5),
        ERROR_BOUND);

    constructParams.clear();
    constructParams.add(3);
    constructParams.add(20);
    gamma = new Gamma(constructParams);
    assertEquals(Double.NEGATIVE_INFINITY, gamma.getLogProb(args, 0),
        ERROR_BOUND);
    assertEquals(Math.log(3.678794411714427), gamma.getLogProb(args, 0.05),
        ERROR_BOUND);
  }

}
