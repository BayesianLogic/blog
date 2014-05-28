/**
 * 
 */
package test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import blog.distrib.Beta;

/**
 * @author cgioia
 * @since May 27, 2014
 * 
 */
public class TestBeta extends TestDistribution {

  @Test
  public void testGetProb() {
    // assuming old implementation works
    constructParams.add(1.0);
    constructParams.add(2.0);
    Beta b = new Beta(constructParams);
    assertEquals(2.0, b.getProb(args, 0), ERROR_BOUND);
    assertEquals(1.0, b.getProb(args, 0.5), ERROR_BOUND);
    assertEquals(0.8, b.getProb(args, 0.6), ERROR_BOUND);
    assertEquals(0.0, b.getProb(args, 1), ERROR_BOUND);

    constructParams.clear();
    constructParams.add(2);
    constructParams.add(2);
    b = new Beta(constructParams);
    assertEquals(0.0, b.getProb(args, 0), ERROR_BOUND);
    assertEquals(1.5, b.getProb(args, 0.5), ERROR_BOUND);
    assertEquals(0.0, b.getProb(args, 1.0), ERROR_BOUND);
  }

  @Test
  public void testGetLogProb() {
    constructParams.add(1.0);
    constructParams.add(2.0);
    Beta b = new Beta(constructParams);
    assertEquals(Math.log(2.0), b.getLogProb(args, 0), ERROR_BOUND);
    assertEquals(Math.log(1.0), b.getLogProb(args, 0.5), ERROR_BOUND);
    assertEquals(Math.log(0.8), b.getLogProb(args, 0.8), ERROR_BOUND);
    assertEquals(Double.NEGATIVE_INFINITY, b.getLogProb(args, 1.0), ERROR_BOUND);

    constructParams.clear();
    constructParams.add(2);
    constructParams.add(2);
    assertEquals(Double.NEGATIVE_INFINITY, b.getLogProb(args, 0), ERROR_BOUND);
    assertEquals(Math.log(1.5), b.getLogProb(args, 0.5), ERROR_BOUND);
    assertEquals(Double.NEGATIVE_INFINITY, b.getLogProb(args, 1.0), ERROR_BOUND);
  }

}
