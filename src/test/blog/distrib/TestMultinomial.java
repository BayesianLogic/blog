/**
 * 
 */
package test.blog.distrib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import blog.common.numerical.MatrixFactory;
import blog.common.numerical.MatrixLib;
import blog.distrib.Multinomial;

/**
 * @author cgioia
 * @since Jun 16, 2014
 * 
 */
public class TestMultinomial implements TestDistributions {
  private final double ERROR = 10e-5;

  /** Multinomial. n = 3, p = [0.5, 0.25, 0.25]. */
  public void testMultinomial1(Multinomial mult) {
    assertEquals(0.1875, mult.getProb(MatrixFactory.createRowVector(1, 1, 1)),
        ERROR);
    assertEquals(Math.log(0.1875),
        mult.getLogProb(MatrixFactory.createRowVector(1, 1, 1)), ERROR);
    assertEquals(0.1875, mult.getProb(MatrixFactory.createRowVector(2, 1, 0)),
        ERROR);
    assertEquals(Math.log(0.1875),
        mult.getLogProb(MatrixFactory.createRowVector(2, 1, 0)), ERROR);
  }

  /** Multinomial. n = 4, p = [0.25, 0.25, 0.25, 0.25]. */
  public void testMultinomial2(Multinomial mult) {
    assertEquals(0.09375,
        mult.getProb(MatrixFactory.createRowVector(1, 1, 1, 1)), ERROR);
    assertEquals(Math.log(0.09375),
        mult.getLogProb(MatrixFactory.createRowVector(1, 1, 1, 1)), ERROR);
    assertEquals(3.90625e-3,
        mult.getProb(MatrixFactory.createRowVector(4, 0, 0, 0)), ERROR);
    assertEquals(Math.log(3.90625e-3),
        mult.getLogProb(MatrixFactory.createRowVector(4, 0, 0, 0)), ERROR);
    assertEquals(0, mult.getProb(MatrixFactory.createRowVector(2, 1, 1, 1)),
        ERROR);
    assertEquals(Double.NEGATIVE_INFINITY,
        mult.getLogProb(MatrixFactory.createRowVector(2, 1, 1, 1)), ERROR);
    assertEquals(0, mult.getProb(MatrixFactory.createRowVector(2, -1, 1, 2)),
        ERROR);
    assertEquals(Double.NEGATIVE_INFINITY,
        mult.getLogProb(MatrixFactory.createRowVector(2, -1, 1, 2)), ERROR);
  }

  @Test
  public void testProbabilityViaConstructor() {
    // no longer needed. will be removed
  }

  @Test
  public void testProbabilityViaSetParams() {
    Multinomial mult = new Multinomial();
    mult.setParams(new Object[] { 3,
        MatrixFactory.createRowVector(0.5, 0.25, 0.25) });
    testMultinomial1(mult);
    mult.setParams(new Object[] { 4,
        MatrixFactory.createRowVector(0.25, 0.25, 0.25, 0.25) });
    testMultinomial2(mult);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments() {
    Multinomial mult = new Multinomial();
    mult.setParams(new Object[] { null,
        MatrixFactory.createRowVector(0.5, 0.25, 0.25) });
    mult.sampleVal();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments2() {
    Multinomial mult = new Multinomial();
    mult.setParams(new Object[] { 2, null });
    mult.sampleVal();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments() {
    Multinomial mult = new Multinomial();
    MatrixLib lib = MatrixFactory.ones(2, 3);
    mult.setParams(new Object[] { 2, lib });
    mult.sampleVal();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments2() {
    Multinomial mult = new Multinomial();
    mult.setParams(new Object[] { -1, MatrixFactory.createRowVector(1, 1) });
    mult.sampleVal();
  }

  @Test
  public void testNormalization() {
    Multinomial mult = new Multinomial();
    mult.setParams(new Object[] { 4, MatrixFactory.createRowVector(1, 1, 1, 1) });
    testMultinomial2(mult);
  }

  @Test
  public void testDoubleSet() {
    Multinomial mult = new Multinomial();
    mult.setParams(new Object[] { null, null });
    mult.setParams(new Object[] { null, MatrixFactory.createRowVector(2, 1, 1) });
    mult.setParams(new Object[] { 3, null });
    testMultinomial1(mult);
    mult.setParams(new Object[] { 4, null });
    mult.setParams(new Object[] { null,
        MatrixFactory.createRowVector(1, 1, 1, 1) });
    testMultinomial2(mult);
  }
}
