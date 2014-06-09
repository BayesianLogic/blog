/**
 * 
 */
package test.blog.distrib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import blog.common.Util;
import blog.distrib.Exponential;

/**
 * Unit Tests for Exponential Distribution
 */
public class TestExponential implements TestDistributions {
  private final double ERROR = 10e-5;

  void testDistributionRun(Exponential dist) {
    dist.getProb(-1);
    dist.getProb(0);
    dist.getProb(10);
    dist.getProb(1000);
  }

  /** Exponential, lambda = 1. */
  public void testExponential1(Exponential exp) {
    assertEquals(1, exp.getLambda(), ERROR);

    assertEquals(0.0, exp.getProb(-20), ERROR);
    assertEquals(0.0, exp.getProb(-0.01), ERROR);
    assertEquals(1.0, exp.getProb(0.0), ERROR);
    assertEquals(0.36787944117144233, exp.getProb(1.0), ERROR);
    assertEquals(0.1353352832366127, exp.getProb(2.0), ERROR);
    assertEquals(0.00408677143846, exp.getProb(5.5), ERROR);

    assertEquals(Double.NEGATIVE_INFINITY, exp.getLogProb(-20), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY, exp.getLogProb(-0.01), ERROR);
    assertEquals(0.0, exp.getLogProb(0.0), ERROR);
    assertEquals(-1, exp.getLogProb(1.0), ERROR);
    assertEquals(-2, exp.getLogProb(2.0), ERROR);
    assertEquals(-5.5, exp.getLogProb(5.5), ERROR);
  }

  /** Exponential, lambda = 5.5. */

  public void testExponential2(Exponential exp) {
    assertEquals(5.5, exp.getLambda(), ERROR);

    assertEquals(0.0, exp.getProb(-20), ERROR);
    assertEquals(0.0, exp.getProb(-0.01), ERROR);
    assertEquals(5.5, exp.getProb(0.0), ERROR);
    assertEquals(3.1732239570926764, exp.getProb(0.1), ERROR);
    assertEquals(0.022477242911552366, exp.getProb(1.0), ERROR);
    assertEquals(2.399904239119036e-08, exp.getProb(3.5), ERROR);
    assertEquals(4.0082482527008306e-13, exp.getProb(5.5), ERROR);

    assertEquals(Double.NEGATIVE_INFINITY, exp.getLogProb(-20), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY, exp.getLogProb(-0.01), ERROR);
    assertEquals(1.7047480922384253, exp.getLogProb(0.0), 10e-2);
    assertEquals(1.154748092238425, exp.getLogProb(0.1), 10e-2);
    assertEquals(-3.7952519077615747, exp.getLogProb(1.0), 10e-2);
    assertEquals(-17.545251907761575, exp.getLogProb(3.5), 10e-2);
    assertEquals(-28.545251907761575, exp.getLogProb(5.5), 10e-2);
  }

  @Test
  public void testProbabilityViaConstructor() {
    // no longer needed. will be removed
  }

  @Test
  public void testProbabilityViaSetParams() {
    Exponential exp = new Exponential();
    exp.setParams(Util.array(1.0));
    testExponential1(exp);
    exp.setParams(Util.array(5.5));
    testExponential2(exp);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments() {
    Exponential exp = new Exponential();
    exp.setParams(Util.array(null));
    testDistributionRun(exp);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments2() {
    Exponential exp = new Exponential();
    Object[] obj = new Object[0];
    exp.setParams(obj);
    testDistributionRun(exp);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExtraneousArguments() {
    Exponential exp = new Exponential();
    exp.setParams(Util.array(1, 2));
    testDistributionRun(exp);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments() {
    Exponential exp = new Exponential();
    exp.setParams(Util.array(0.0));
    testDistributionRun(exp);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments2() {
    Exponential exp = new Exponential();
    exp.setParams(Util.array(-1.0));
    testDistributionRun(exp);
  }

  @Test
  public void testDoubleSet() {
    Exponential exp = new Exponential();
    exp.setParams(Util.array(1.0));
    exp.setParams(Util.array(null));
    testExponential1(exp);
    exp.setParams(Util.array(2.0));
    exp.setParams(Util.array(5.5));
    testExponential2(exp);

  }

}
