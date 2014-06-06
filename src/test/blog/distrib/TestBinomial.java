/**
 * 
 */
package test.blog.distrib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import blog.common.Util;
import blog.distrib.Binomial;

/**
 * Unit Tests for Binomial Distribution
 */
public class TestBinomial implements TestDistributions {
  private final double ERROR = 10e-5;

  void testDistributionRun(Binomial dist) {
    dist.getProb(-1);
    dist.getProb(0);
    dist.getProb(10);
    dist.getProb(1000);
  }

  /** Binomial, n = 2, p = 0.5 **/
  public void testBinomial1(Binomial binomial) {
    assertEquals(2, binomial.getN(), ERROR);
    assertEquals(0.5, binomial.getP(), ERROR);

    assertEquals(0, binomial.getProb(-20), ERROR);
    assertEquals(0, binomial.getProb(-1), ERROR);
    assertEquals(0.25, binomial.getProb(0), ERROR);
    assertEquals(0.5, binomial.getProb(1), ERROR);
    assertEquals(0.25, binomial.getProb(2), ERROR);
    assertEquals(0, binomial.getProb(3), ERROR);
    assertEquals(0, binomial.getProb(20), ERROR);

    assertEquals(Double.NEGATIVE_INFINITY, binomial.getLogProb(-20), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY, binomial.getLogProb(-1), ERROR);
    assertEquals(Math.log(0.25), binomial.getLogProb(0), ERROR);
    assertEquals(Math.log(0.5), binomial.getLogProb(1), ERROR);
    assertEquals(Math.log(0.25), binomial.getLogProb(2), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY, binomial.getLogProb(3), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY, binomial.getLogProb(20), ERROR);
  }

  /** Binomial, n = 2, p = 0.6 **/
  public void testBinomial2(Binomial binomial) {
    assertEquals(2, binomial.getN(), ERROR);
    assertEquals(0.6, binomial.getP(), ERROR);

    assertEquals(0, binomial.getProb(-20), ERROR);
    assertEquals(0, binomial.getProb(-1), ERROR);
    assertEquals(0.16, binomial.getProb(0), ERROR);
    assertEquals(0.48, binomial.getProb(1), ERROR);
    assertEquals(0.36, binomial.getProb(2), ERROR);
    assertEquals(0, binomial.getProb(3), ERROR);
    assertEquals(0, binomial.getProb(20), ERROR);

    assertEquals(Double.NEGATIVE_INFINITY, binomial.getLogProb(-20), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY, binomial.getLogProb(-1), ERROR);
    assertEquals(Math.log(0.16), binomial.getLogProb(0), ERROR);
    assertEquals(Math.log(0.48), binomial.getLogProb(1), ERROR);
    assertEquals(Math.log(0.36), binomial.getLogProb(2), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY, binomial.getLogProb(3), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY, binomial.getLogProb(20), ERROR);
  }

  /** Binomial, n = 5, p = 0.6 **/
  public void testBinomial3(Binomial binomial) {
    assertEquals(5, binomial.getN(), ERROR);
    assertEquals(0.6, binomial.getP(), ERROR);

    assertEquals(0.010240000000000008, binomial.getProb(0), ERROR);
    assertEquals(0.076800000000000007, binomial.getProb(1), ERROR);
    assertEquals(0.23039999999999999, binomial.getProb(2), ERROR);
    assertEquals(0.34559999999999991, binomial.getProb(3), ERROR);
    assertEquals(0.25919999999999987, binomial.getProb(4), ERROR);
    assertEquals(0.077759999999999996, binomial.getProb(5), ERROR);

    assertEquals(-4.5814536593707746, binomial.getLogProb(0), ERROR);
    assertEquals(-2.5665506388285104, binomial.getLogProb(1), ERROR);
    assertEquals(-1.4679383501604009, binomial.getLogProb(2), ERROR);
    assertEquals(-1.0624732420522367, binomial.getLogProb(3), ERROR);
    assertEquals(-1.3501553145040179, binomial.getLogProb(4), ERROR);
    assertEquals(-2.5541281188299534, binomial.getLogProb(5), ERROR);
  }

  @Test
  public void testProbabilityViaConstructor() {
    Binomial bin = new Binomial(2, 0.5);
    testBinomial1(bin);
    bin = new Binomial(2, 0.6);
    testBinomial2(bin);
    bin = new Binomial(5, 0.6);
    testBinomial3(bin);
  }

  @Test
  public void testProbabilityViaSetParams() {
    Binomial bin = new Binomial();
    bin.setParams(Util.array(2, 0.5));
    testBinomial1(bin);
    bin.setParams(Util.array(2, 0.6));
    testBinomial2(bin);
    bin.setParams(Util.array(5, 0.6));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments() {
    Binomial bin = new Binomial();
    bin.setParams(Util.array(2, null));
    testDistributionRun(bin);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments2() {
    Binomial bin = new Binomial();
    bin.setParams(Util.array(null, 0.5));
    testDistributionRun(bin);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments3() {
    Binomial bin = new Binomial();
    bin.setParams(Util.array(null, null));
    testDistributionRun(bin);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments4() {
    Binomial bin = new Binomial();
    bin.setParams(Util.array(2));
    testDistributionRun(bin);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments5() {
    Binomial bin = new Binomial();
    bin.setParams(Util.array(1, 0.5, 3));
    testDistributionRun(bin);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments() {
    Binomial bin = new Binomial();
    bin.setParams(Util.array(1, -0.01));
    testDistributionRun(bin);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments2() {
    Binomial bin = new Binomial();
    bin.setParams(Util.array(1, 1.01));
    testDistributionRun(bin);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments3() {
    Binomial bin = new Binomial();
    bin.setParams(Util.array(-1, 0.25));
    testDistributionRun(bin);
  }

  @Test(expected = ClassCastException.class)
  public void testIncorrectArguments4() {
    Binomial bin = new Binomial();
    bin.setParams(Util.array(1.5, 0.25));
    testDistributionRun(bin);
  }

  @Test
  public void testDoubleSet() {
    Binomial bin = new Binomial();
    bin.setParams(null, 0.5);
    bin.setParams(2, 0.5);
    testBinomial1(bin);
    bin.setParams(null, 0.6);
    testBinomial2(bin);
    bin.setParams(5, null);
    testBinomial3(bin);
  }

}
