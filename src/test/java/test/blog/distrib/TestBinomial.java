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
    // no longer needed. will be removed.
  }

  @Test
  public void testProbabilityViaSetParams() {
    Binomial bin = new Binomial();
    bin.setParams(new Object[] { 2, 0.5 });
    testBinomial1(bin);
    bin.setParams(new Object[] { 2, 0.6 });
    testBinomial2(bin);
    bin.setParams(new Object[] { 5, 0.6 });
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments() {
    Binomial bin = new Binomial();
    bin.setParams(new Object[] { 2, null });
    testDistributionRun(bin);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments2() {
    Binomial bin = new Binomial();
    bin.setParams(new Object[] { null, 0.5 });
    testDistributionRun(bin);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments3() {
    Binomial bin = new Binomial();
    bin.setParams(new Object[] { null, null });
    testDistributionRun(bin);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments4() {
    Binomial bin = new Binomial();
    bin.setParams(new Object[] { 2 });
    testDistributionRun(bin);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments5() {
    Binomial bin = new Binomial();
    bin.setParams(new Object[] { 1, 0.5, 3 });
    testDistributionRun(bin);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments() {
    Binomial bin = new Binomial();
    bin.setParams(new Object[] { 1, -0.01 });
    testDistributionRun(bin);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments2() {
    Binomial bin = new Binomial();
    bin.setParams(new Object[] { 1, 1.01 });
    testDistributionRun(bin);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments3() {
    Binomial bin = new Binomial();
    bin.setParams(new Object[] { -1, 0.25 });
    testDistributionRun(bin);
  }

  @Test(expected = ClassCastException.class)
  public void testIncorrectArguments4() {
    Binomial bin = new Binomial();
    bin.setParams(new Object[] { 1.5, 0.25 });
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

  @Test
  public void testSetParamsIntegerArguments() {
    Binomial bin = new Binomial();
    bin.setParams(new Object[] { 2, 1 });
    assertEquals(0.0, bin.getProb(0), ERROR);
    assertEquals(0.0, bin.getProb(1), ERROR);
    assertEquals(1.0, bin.getProb(2), ERROR);
    bin.setParams(new Object[] { 3, 0 });
    assertEquals(1.0, bin.getProb(0), ERROR);
    assertEquals(0.0, bin.getProb(1), ERROR);
    assertEquals(0.0, bin.getProb(2), ERROR);
    assertEquals(0.0, bin.getProb(3), ERROR);
  }

  @Test
  public void testGetProbIntegerArguments() {
    // not needed
  }

  @Test
  public void testGetFiniteSupport() {
    Binomial b = new Binomial();
    b.setParams(10, 0.5);
    assertEquals(11, b.getFiniteSupport().length);
    for (int i = 0; i <= 10; i++) {
      assertEquals(b.getFiniteSupport()[i], i);
    }
  }

}
