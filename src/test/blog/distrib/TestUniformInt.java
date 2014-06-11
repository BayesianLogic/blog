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
public class TestUniformInt extends TestDistribution {

  @Test
  public void testGetProb() {
    constructParams.add(2);
    constructParams.add(6);
    UniformInt unif = new UniformInt(constructParams);
    assertEquals(0.0, unif.getProb(args, 1), ERROR_BOUND);
    assertEquals(0.2, unif.getProb(args, 2), ERROR_BOUND);
    assertEquals(0.2, unif.getProb(args, 4), ERROR_BOUND);
    assertEquals(0.2, unif.getProb(args, 6), ERROR_BOUND);
    assertEquals(0.0, unif.getProb(args, 7), ERROR_BOUND);

    constructParams.clear();
    constructParams.add(3);
    constructParams.add(3);
    unif = new UniformInt(constructParams);
    assertEquals(1, unif.getProb(args, 3), ERROR_BOUND);
    assertEquals(0, unif.getProb(args, 4), ERROR_BOUND);
  }

  @Test
  public void testGetLogProb() {
    constructParams.add(2);
    constructParams.add(6);
    UniformInt unif = new UniformInt(constructParams);
    assertEquals(Double.NEGATIVE_INFINITY, unif.getLogProb(args, 1),
        ERROR_BOUND);
    assertEquals(Math.log(0.2), unif.getLogProb(args, 2), ERROR_BOUND);
    assertEquals(Math.log(0.2), unif.getLogProb(args, 4), ERROR_BOUND);
    assertEquals(Math.log(0.2), unif.getLogProb(args, 6), ERROR_BOUND);
    assertEquals(Double.NEGATIVE_INFINITY, unif.getLogProb(args, 7),
        ERROR_BOUND);

    constructParams.clear();
    constructParams.add(3);
    constructParams.add(3);
    unif = new UniformInt(constructParams);
    assertEquals(0, unif.getLogProb(args, 3), ERROR_BOUND);
    assertEquals(Double.NEGATIVE_INFINITY, unif.getLogProb(args, 4),
        ERROR_BOUND);
  }

}
