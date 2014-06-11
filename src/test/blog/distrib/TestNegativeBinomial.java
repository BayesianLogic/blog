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
public class TestNegativeBinomial implements TestDistributions {
  private final double ERROR = 10e-5;

  public void testDistributionRun(NegativeBinomial neg) {
    neg.getProb(-1);
    neg.getProb(0);
    neg.getProb(1);
    neg.getProb(1000);
  }

  /** Negative Binomial. r = 1, p = 0.6 . */
  public void testNegativeBinomial1(NegativeBinomial neg) {
    assertEquals(1, neg.getR(), ERROR);
    assertEquals(0.6, neg.getP(), ERROR);

    assertEquals(0, neg.getProb(-1), ERROR);
    assertEquals(0.4, neg.getProb(0), ERROR);
    assertEquals(0.24, neg.getProb(1), ERROR);
    assertEquals(0.144, neg.getProb(2), ERROR);
    assertEquals(0.0024186470399999993, neg.getProb(10), ERROR);

    assertEquals(Double.NEGATIVE_INFINITY, neg.getLogProb(-1), ERROR);
    assertEquals(-0.916290731874155, neg.getLogProb(0), ERROR);
    assertEquals(-1.4271163556401458, neg.getLogProb(1), ERROR);
    assertEquals(-6.024546969534062, neg.getLogProb(10), ERROR);
  }

  /** Negative Binomial. r = 5, p = 0.6 . */
  public void testNegativeBinomial2(NegativeBinomial neg) {
    assertEquals(5, neg.getR(), ERROR);
    assertEquals(0.6, neg.getP(), ERROR);

    assertEquals(Double.NEGATIVE_INFINITY, neg.getLogProb(-1), ERROR);
    assertEquals(-4.581453659370775, neg.getLogProb(0), ERROR);
    assertEquals(-3.4828413707026655, neg.getLogProb(1), ERROR);
    assertEquals(-2.895054706, neg.getLogProb(2), ERROR);
    assertEquals(-2.2992998712492505, neg.getLogProb(5), ERROR);
    assertEquals(-2.7809551177154614, neg.getLogProb(10), ERROR);
    assertEquals(-5.526907027679702, neg.getLogProb(20), ERROR);

    assertEquals(0, neg.getProb(-1), ERROR);
    assertEquals(0.010240000000000003, neg.getProb(0), ERROR);
    assertEquals(0.030720000000000004, neg.getProb(1), ERROR);
    assertEquals(0.05529600000000001, neg.getProb(2), ERROR);
    assertEquals(0.10032906240000002, neg.getProb(5), ERROR);
    assertEquals(0.06197928158822401, neg.getProb(10), ERROR);
    assertEquals(0.003978274773412775, neg.getProb(20), ERROR);

  }

  @Test
  public void testProbabilityViaConstructor() {
    // no longer needed
  }

  @Test
  public void testProbabilityViaSetParams() {
    NegativeBinomial nb = new NegativeBinomial();
    nb.setParams(new Object[] { 1, 0.6 });
    testNegativeBinomial1(nb);
    nb.setParams(new Object[] { 5, 0.6 });
    testNegativeBinomial2(nb);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments() {
    NegativeBinomial nb = new NegativeBinomial();
    nb.setParams(new Object[] { 1 });
    testDistributionRun(nb);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments2() {
    NegativeBinomial nb = new NegativeBinomial();
    nb.setParams(new Object[] { 1, null });
    testDistributionRun(nb);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExtraneousArguments() {
    NegativeBinomial nb = new NegativeBinomial();
    nb.setParams(new Object[] { 1, 0.6, 0.2 });
    testDistributionRun(nb);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments() {
    NegativeBinomial nb = new NegativeBinomial();
    nb.setParams(new Object[] { 1, -0.1 });
    testDistributionRun(nb);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments2() {
    NegativeBinomial nb = new NegativeBinomial();
    nb.setParams(new Object[] { 1, 1.01 });
    testDistributionRun(nb);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments3() {
    NegativeBinomial nb = new NegativeBinomial();
    nb.setParams(new Object[] { 0, 0.5 });
    testDistributionRun(nb);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments4() {
    NegativeBinomial nb = new NegativeBinomial();
    nb.setParams(new Object[] { -1, 0.5 });
    testDistributionRun(nb);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments5() {
    NegativeBinomial nb = new NegativeBinomial();
    nb.setParams(new Object[] { 1, -0.1 });
    testDistributionRun(nb);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments6() {
    NegativeBinomial nb = new NegativeBinomial();
    nb.setParams(new Object[] { 1, 1.0 });
    testDistributionRun(nb);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments7() {
    NegativeBinomial nb = new NegativeBinomial();
    nb.setParams(new Object[] { 3, 0.0 });
    testDistributionRun(nb);
  }

  @Test
  public void testDoubleSet() {
    NegativeBinomial nb = new NegativeBinomial();
    nb.setParams(new Object[] { 1, null });
    nb.setParams(new Object[] { null, 0.6 });
    testNegativeBinomial1(nb);
    nb.setParams(new Object[] { 5, null });
    nb.setParams(new Object[] { null, null });
    testNegativeBinomial2(nb);
  }

}
