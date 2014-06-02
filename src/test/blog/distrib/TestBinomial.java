/**
 * 
 */
package test.blog.distrib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import blog.distrib.Binomial;

/**
 * Unit Tests for Binomial Distribution
 */
public class TestBinomial extends TestDistribution {

  @Test
  public void testGetProb() {
    constructParams.add(5);
    constructParams.add(1.0);
    Binomial bin = new Binomial(constructParams);
    assertEquals(1.0, bin.getProb(args, 5), ERROR_BOUND);
    assertEquals(0.0, bin.getProb(args, 4), ERROR_BOUND);

    constructParams.clear();
    constructParams.add(4);
    constructParams.add(0.6);
    bin = new Binomial(constructParams);
    assertEquals(0.025600000000000008, bin.getProb(args, 0), ERROR_BOUND);
    assertEquals(0.15360000000000007, bin.getProb(args, 1), ERROR_BOUND);
    assertEquals(0.34560000000000007, bin.getProb(args, 2), ERROR_BOUND);
    assertEquals(0.34560000000000007, bin.getProb(args, 3), ERROR_BOUND);
    assertEquals(0.12959999999999999, bin.getProb(args, 4), ERROR_BOUND);
    assertEquals(0.0, bin.getProb(args, 5), ERROR_BOUND);

    // Passing in n (# of trials) as a random parameter
    constructParams.clear();
    constructParams.add(0.6);
    bin = new Binomial(constructParams);
    args.add(4);
    assertEquals(0.34560000000000007, bin.getProb(args, 2), ERROR_BOUND);
  }

  @Test
  public void testGetLogProb() {
    constructParams.add(5);
    constructParams.add(1.0);
    Binomial bin = new Binomial(constructParams);
    assertEquals(0, bin.getLogProb(args, 5), ERROR_BOUND);
    assertEquals(Double.NEGATIVE_INFINITY, bin.getLogProb(args, 4), ERROR_BOUND);

    constructParams.clear();
    constructParams.add(4);
    constructParams.add(0.6);
    bin = new Binomial(constructParams);
    assertEquals(Math.log(0.025600000000000008), bin.getLogProb(args, 0),
        ERROR_BOUND);
    assertEquals(Math.log(0.15360000000000007), bin.getLogProb(args, 1),
        ERROR_BOUND);
    assertEquals(Math.log(0.34560000000000007), bin.getLogProb(args, 2),
        ERROR_BOUND);
    assertEquals(Math.log(0.34560000000000007), bin.getLogProb(args, 3),
        ERROR_BOUND);
    assertEquals(Math.log(0.12959999999999999), bin.getLogProb(args, 4),
        ERROR_BOUND);
    assertEquals(Double.NEGATIVE_INFINITY, bin.getLogProb(args, 5), ERROR_BOUND);

    // Passing in n (# of trials) as a random parameter
    constructParams.clear();
    constructParams.add(0.6);
    bin = new Binomial(constructParams);
    args.add(4);
    assertEquals(Math.log(0.34560000000000007), bin.getLogProb(args, 2),
        ERROR_BOUND);
  }

}
