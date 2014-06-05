/**
 * 
 */
package test.blog.distrib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import blog.distrib.UniformReal;

/**
 * Unit Tests for Uniform Real
 */
public class TestUniformReal implements TestDistributions {
  private final double ERROR = 1e-10;
  private final double EPSILON = 1e-2;

  @Override
  @Test
  public void testProbabilityViaConstructor() {
    // TODO Auto-generated method stub
    UniformReal unif = new UniformReal(1.0, 3.0);
    testDistribution(unif);
  }

  @Override
  @Test
  public void testProbabilityViaSetParams() {
    UniformReal unif = new UniformReal();
    Double[] ary = new Double[2];
    ary[0] = 1.0;
    ary[1] = 3.0;
    unif.setParams(ary);
    testDistribution(unif);
  }

  @Override
  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments() {
    UniformReal unif = new UniformReal();
    unif.setParams(1.0, null);
    testDistribution(unif);
  }

  @Override
  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments() {
    UniformReal unif = new UniformReal();
    Double[] ary = new Double[2];
    unif.setParams(2.0, 1.9);
    testDistribution(unif);
  }

  @Override
  @Test
  public void testDoubleSet() {
    // TODO Auto-generated method stub
    UniformReal unif = new UniformReal();
    unif.setParams(null, 1.0);
    unif.setParams(0.0, null);
    testDistribution(unif);
  }

  public void testDistribution(UniformReal unif) {
    double lower = unif.getLower();
    double upper = unif.getUpper();
    double prob = (1.0 / (upper - lower));
    assertEquals(0, unif.getProb(lower - EPSILON), ERROR);
    assertEquals(prob, unif.getProb(lower), ERROR);
    assertEquals(prob, unif.getProb((lower + upper) / 2.0), ERROR);
    assertEquals(0, unif.getProb(upper), ERROR);
    assertEquals(0, unif.getProb(upper + EPSILON), ERROR);

    assertEquals(Double.NEGATIVE_INFINITY, unif.getLogProb(lower - EPSILON),
        ERROR);
    assertEquals(Math.log(prob), unif.getLogProb(lower), ERROR);
    assertEquals(Math.log(prob), unif.getLogProb((lower + upper) / 2.0), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY, unif.getLogProb(upper), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY, unif.getLogProb(upper + EPSILON),
        ERROR);
  }

}
