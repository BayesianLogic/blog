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
    assertEquals(0.1875, mult.getProb(new Integer[] { 1, 1, 1 }), ERROR);
    assertEquals(Math.log(0.1875), mult.getLogProb(new Integer[] { 1, 1, 1 }),
        ERROR);
    assertEquals(0.1875, mult.getProb(new Integer[] { 2, 1, 0 }), ERROR);
    assertEquals(Math.log(0.1875), mult.getLogProb(new Integer[] { 2, 1, 0 }),
        ERROR);
  }

  /** Multinomial. n = 4, p = [0.25, 0.25, 0.25, 0.25]. */
  public void testMultinomial2(Multinomial mult) {
    assertEquals(0.09375, mult.getProb(new Integer[] { 1, 1, 1, 1 }), ERROR);
    assertEquals(Math.log(0.09375),
        mult.getLogProb(new Integer[] { 1, 1, 1, 1 }), ERROR);
    assertEquals(3.90625e-3, mult.getProb(new Integer[] { 4, 0, 0, 0 }), ERROR);
    assertEquals(Math.log(3.90625e-3),
        mult.getLogProb(new Integer[] { 4, 0, 0, 0 }), ERROR);
    assertEquals(0, mult.getProb(new Integer[] { 2, 1, 1, 1 }), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY,
        mult.getLogProb(new Integer[] { 2, 1, 1, 1 }), ERROR);
    assertEquals(0, mult.getProb(new Integer[] { 2, -1, 1, 2 }), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY,
        mult.getLogProb(new Integer[] { 2, -1, 1, 2 }), ERROR);
  }

  @Test
  public void testProbabilityViaConstructor() {
    // no longer needed. will be removed
  }

  @Test
  public void testProbabilityViaSetParams() {
    Multinomial mult = new Multinomial();
    mult.setParams(new Object[] { 3,
        MatrixFactory.createColumnVector(0.5, 0.25, 0.25) });
    testMultinomial1(mult);
    mult.setParams(new Object[] { 4,
        MatrixFactory.createColumnVector(0.25, 0.25, 0.25, 0.25) });
    testMultinomial2(mult);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments() {
    Multinomial mult = new Multinomial();
    mult.setParams(new Object[] { null,
        MatrixFactory.createColumnVector(0.5, 0.25, 0.25) });
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
    mult.setParams(new Object[] { -1, MatrixFactory.createColumnVector(1, 1) });
    mult.sampleVal();
  }

  @Test
  public void testNormalization() {
    Multinomial mult = new Multinomial();
    mult.setParams(new Object[] { 4,
        MatrixFactory.createColumnVector(1, 1, 1, 1) });
    testMultinomial2(mult);
  }

  @Test
  public void testDoubleSet() {
    Multinomial mult = new Multinomial();
    mult.setParams(new Object[] { null, null });
    mult.setParams(new Object[] { null,
        MatrixFactory.createColumnVector(2, 1, 1) });
    mult.setParams(new Object[] { 3, null });
    testMultinomial1(mult);
    mult.setParams(new Object[] { 4, null });
    mult.setParams(new Object[] { null,
        MatrixFactory.createColumnVector(1, 1, 1, 1) });
    testMultinomial2(mult);
  }

  @Test
  public void testSetParamsIntegerArguments() {
    // not needed
  }

  @Test
  public void testGetProbIntegerArguments() {
    // not needed
  }

  @Test
  public void testGetFiniteSupport() {
    Multinomial mult = new Multinomial();
    mult.setParams(2, MatrixFactory.createColumnVector(0.3, 0, 0.7));
    Object[] list = mult.getFiniteSupport();
    assertEquals(3, list.length);
    assertEquals(list[0], MatrixFactory.createColumnVector(0, 0, 2));
    assertEquals(list[1], MatrixFactory.createColumnVector(1, 0, 1));
    assertEquals(list[2], MatrixFactory.createColumnVector(2, 0, 0));

    mult = new Multinomial();
    mult.setParams(3, MatrixFactory.createColumnVector(0.3, 0.3, 0.6));
    list = mult.getFiniteSupport();
    assertEquals(10, list.length);

    assertEquals(list[0], MatrixFactory.createColumnVector(0, 0, 3));
    assertEquals(list[1], MatrixFactory.createColumnVector(0, 1, 2));
    assertEquals(list[2], MatrixFactory.createColumnVector(0, 2, 1));
    assertEquals(list[3], MatrixFactory.createColumnVector(0, 3, 0));
    assertEquals(list[4], MatrixFactory.createColumnVector(1, 0, 2));
    assertEquals(list[5], MatrixFactory.createColumnVector(1, 1, 1));
    assertEquals(list[6], MatrixFactory.createColumnVector(1, 2, 0));
    assertEquals(list[7], MatrixFactory.createColumnVector(2, 0, 1));
    assertEquals(list[8], MatrixFactory.createColumnVector(2, 1, 0));
    assertEquals(list[9], MatrixFactory.createColumnVector(3, 0, 0));
  }
}
