/**
 * 
 */
package test.blog.distrib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import blog.distrib.BooleanDistrib;

/**
 * Unit Tests for Boolean Distribution
 */
public class TestBooleanDistrib extends TestDistribution {

  @Test
  public void testGetProbFixed() {
    constructParams.add(0.6);
    BooleanDistrib bool = new BooleanDistrib(constructParams);
    assertEquals(0.6, bool.getProb(args, true), ERROR_BOUND);
    assertEquals(0.4, bool.getProb(args, false), ERROR_BOUND);
    constructParams.clear();
    constructParams.add(0);
    bool = new BooleanDistrib(constructParams);
    assertEquals(0.0, bool.getProb(args, true), ERROR_BOUND);
    assertEquals(1.0, bool.getProb(args, false), ERROR_BOUND);
  }

  @Test
  public void testGetProbRandom() {
    BooleanDistrib bool = new BooleanDistrib(constructParams);
    args.add(0.6);
    assertEquals(0.6, bool.getProb(args, true), ERROR_BOUND);
    assertEquals(0.4, bool.getProb(args, false), ERROR_BOUND);
    args.clear();
    args.add(0);
    assertEquals(0.0, bool.getProb(args, true), ERROR_BOUND);
    assertEquals(1.0, bool.getProb(args, false), ERROR_BOUND);
  }

  @Test
  public void testGetLogProbFixed() {
    constructParams.add(0.6);
    BooleanDistrib bool = new BooleanDistrib(constructParams);
    assertEquals(Math.log(0.6), bool.getLogProb(args, true), ERROR_BOUND);
    assertEquals(Math.log(0.4), bool.getLogProb(args, false), ERROR_BOUND);
    constructParams.clear();
    constructParams.add(0);
    bool = new BooleanDistrib(constructParams);
    assertEquals(Double.NEGATIVE_INFINITY, bool.getLogProb(args, true),
        ERROR_BOUND);
    assertEquals(0, bool.getLogProb(args, false), ERROR_BOUND);
  }

  @Test
  public void testGetLogProbRandom() {
    BooleanDistrib bool = new BooleanDistrib(constructParams);
    args.add(0.6);
    assertEquals(Math.log(0.6), bool.getLogProb(args, true), ERROR_BOUND);
    assertEquals(Math.log(0.4), bool.getLogProb(args, false), ERROR_BOUND);
    args.clear();
    args.add(0);
    assertEquals(Double.NEGATIVE_INFINITY, bool.getLogProb(args, true),
        ERROR_BOUND);
    assertEquals(0, bool.getLogProb(args, false), ERROR_BOUND);
  }

}
