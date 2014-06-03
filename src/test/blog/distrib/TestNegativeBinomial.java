/**
 * 
 */
package test.blog.distrib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import blog.distrib.NegativeBinomial;

/**
 * Unit Tests for Negative Binomial
 */
public class TestNegativeBinomial extends TestDistribution {

  @Test
  public void testGetProb() {
    constructParams.add(2);
    constructParams.add(0.2);
    NegativeBinomial nb = new NegativeBinomial(constructParams);
    assertEquals(0.04, nb.getProb(args, 0), ERROR_BOUND);
    assertEquals(0.064, nb.getProb(args, 1), ERROR_BOUND);
    assertEquals(0.0768, nb.getProb(args, 2), ERROR_BOUND);
    assertEquals(0.04724464, nb.getProb(args, 10), ERROR_BOUND);
  }

  @Test
  public void testGetLogProb() {
    constructParams.add(2);
    constructParams.add(0.2);
    NegativeBinomial nb = new NegativeBinomial(constructParams);
    assertEquals(Math.log(0.04), nb.getLogProb(args, 0), ERROR_BOUND);
    assertEquals(Math.log(0.064), nb.getLogProb(args, 1), ERROR_BOUND);
    assertEquals(Math.log(0.0768), nb.getLogProb(args, 2), ERROR_BOUND);
    assertEquals(Math.log(0.04724464), nb.getLogProb(args, 10), ERROR_BOUND);
  }

}
