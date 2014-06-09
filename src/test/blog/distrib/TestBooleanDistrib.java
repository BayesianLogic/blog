/**
 * 
 */
package test.blog.distrib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import blog.common.Util;
import blog.distrib.BooleanDistrib;

/**
 * Unit Tests for Boolean Distribution
 */
public class TestBooleanDistrib implements TestDistributions {
  private final double ERROR = 10e-5;

  public void testDistributionRun(BooleanDistrib bool) {
    bool.getProb(true);
    bool.getProb(false);
    bool.getLogProb(true);
    bool.getLogProb(false);
  }

  /** Testing a Boolean Distribution with p = 1.0 */
  public void testBooleanDistrib1(BooleanDistrib bool) {
    assertEquals(1.0, bool.getP(), ERROR);

    assertEquals(1.0, bool.getProb(true), ERROR);
    assertEquals(0.0, bool.getProb(false), ERROR);
    assertEquals(0.0, bool.getLogProb(true), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY, bool.getLogProb(false), ERROR);
  }

  /** Testing a Boolean Distribution with p = 0.6 */
  public void testBooleanDistrib2(BooleanDistrib bool) {
    assertEquals(0.6, bool.getP(), ERROR);

    assertEquals(0.6, bool.getProb(true), ERROR);
    assertEquals(0.4, bool.getProb(false), ERROR);
    assertEquals(Math.log(0.6), bool.getLogProb(true), ERROR);
    assertEquals(Math.log(0.4), bool.getLogProb(false), ERROR);
  }

  @Test
  public void testProbabilityViaConstructor() {
    // not needed. will be removed
  }

  @Test
  public void testProbabilityViaSetParams() {
    BooleanDistrib bool = new BooleanDistrib();
    bool.setParams(Util.array(1.0));
    testBooleanDistrib1(bool);
    bool.setParams(Util.array(0.6));
    testBooleanDistrib2(bool);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments() {
    BooleanDistrib bool = new BooleanDistrib();
    bool.setParams(Util.array(null));
    testDistributionRun(bool);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments2() {
    BooleanDistrib bool = new BooleanDistrib();
    Object[] params = new Object[0];
    bool.setParams(params);
    testDistributionRun(bool);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExtraneousArguments() {
    BooleanDistrib bool = new BooleanDistrib();
    bool.setParams(Util.array(0.6, 0.2));
    testDistributionRun(bool);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments() {
    BooleanDistrib bool = new BooleanDistrib();
    bool.setParams(Util.array(1.1));
    testDistributionRun(bool);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments2() {
    BooleanDistrib bool = new BooleanDistrib();
    bool.setParams(Util.array(-1.0));
    testDistributionRun(bool);
  }

  @Test
  public void testDoubleSet() {
    BooleanDistrib bool = new BooleanDistrib();
    bool.setParams(Util.array(null));
    bool.setParams(Util.array(1.0));
    testBooleanDistrib1(bool);
    bool.setParams(Util.array(null));
    testBooleanDistrib1(bool);
  }

}
