/**
 * 
 */
package test.blog.distrib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;


import blog.distrib.Bernoulli;

/**
 * Unit Tests for Bernoulli Distribution
 */
public class TestBernoulli extends TestDistribution {

  @Test
  public void testCorrect() {
    constructParams.add(0.6);
    testBernoulli(constructParams, args);

    constructParams.clear();
    constructParams.add(0);
    testBernoulli(constructParams, args);

    constructParams.clear();
    constructParams.add(1);
    testBernoulli(constructParams, args);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNotProbabilityUnder() {
    constructParams.add(-1);
    testBernoulli(constructParams, args);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNotProbabilityOver() {
    constructParams.add(1.5);
    testBernoulli(constructParams, args);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNoProbability() {
    testBernoulli(constructParams, args);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExtraneousArgs() {
    constructParams.add(0.5);
    args.add(1);
    testBernoulli(constructParams, args);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExtraneousConstructParams() {
    constructParams.add(0.5);
    constructParams.add(0.7);
    testBernoulli(constructParams, args);
  }

  public void testBernoulli(List<Object> constructParams, List<Object> args) {
    Bernoulli b = new Bernoulli(constructParams);
    double probTrue = b.getProb(args, 1);
    double probFalse = b.getProb(args, 0);
    double logProbTrue = b.getLogProb(args, 1);
    double logProbFalse = b.getLogProb(args, 0);
    if (args.size() != 0 || constructParams.size() != 1) {
      // this line should never be reached as getProb should
      // throw an IllegalArgumentException
      assertTrue(false);
    }
    if (!(constructParams.get(0) instanceof Number)) {
      assertTrue(false);
    }
    double p = ((Number) constructParams.get(0)).doubleValue();
    assertEquals(probTrue, p, ERROR_BOUND);
    assertEquals(probFalse, 1 - p, ERROR_BOUND);
    assertEquals(logProbTrue, Math.log(p), ERROR_BOUND);
    assertEquals(logProbFalse, Math.log(1 - p), ERROR_BOUND);

  }

}
