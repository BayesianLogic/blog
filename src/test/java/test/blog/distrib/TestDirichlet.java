/**
 * 
 */
package test.blog.distrib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;

import blog.common.Util;
import blog.common.numerical.MatrixFactory;
import blog.common.numerical.MatrixLib;
import blog.distrib.Dirichlet;

/**
 * Unit Tests for Dirichlet
 */
public class TestDirichlet implements TestDistributions {
  private final double ERROR = 10e-3;

  /**
   * Calling getProb and getLogProb with the argument <code>x</code> on the
   * Dirichlet distribution <code>d</code> should throw an
   * IllegalArgumentException, and sampleValue should throw a
   * IllegalArgumentException.
   * 
   * @param d
   * @param x
   */
  private void shouldThrowExceptions(Dirichlet d, MatrixLib x) {
    try {
      d.getProb(x);
      fail();
    } catch (IllegalArgumentException ex) {
    }
    try {
      d.getLogProb(x);
      fail();
    } catch (IllegalArgumentException ex) {
    }
    try {
      d.sampleVal();
      fail();
    } catch (IllegalArgumentException ex) {
    }
  }

  /** Dirichlet, alpha = [1, 1]. */
  public void testDirichlet1(Dirichlet d) {
    assertEquals(1.0, d.getProb(MatrixFactory.createColumnVector(0.0, 1.0)),
        ERROR);
    assertEquals(1.0, d.getProb(MatrixFactory.createColumnVector(0.5, 0.5)),
        ERROR);
    assertEquals(1.0, d.getProb(MatrixFactory.createColumnVector(1.0, 0.0)),
        ERROR);
    assertEquals(0.0, d.getProb(MatrixFactory.createColumnVector(1.1, 0.0)),
        ERROR);
    assertEquals(0.0, d.getProb(MatrixFactory.createColumnVector(0.4, 0.7)),
        ERROR);

    assertEquals(0.0, d.getLogProb(MatrixFactory.createColumnVector(0.0, 1.0)),
        ERROR);
    assertEquals(0.0, d.getLogProb(MatrixFactory.createColumnVector(0.5, 0.5)),
        ERROR);
    assertEquals(0.0, d.getLogProb(MatrixFactory.createColumnVector(1.0, 0.0)),
        ERROR);
    assertEquals(Double.NEGATIVE_INFINITY,
        d.getLogProb(MatrixFactory.createColumnVector(1.1, 0.0)), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY,
        d.getLogProb(MatrixFactory.createColumnVector(0.4, 0.7)), ERROR);
  }

  /** Dirichlet, alpha = [2, 1, 1]. */
  public void testDirichlet2(Dirichlet d) {
    assertEquals(0.0,
        d.getProb(MatrixFactory.createColumnVector(0.0, 0.5, 0.5)), ERROR);
    assertEquals(1.5,
        d.getProb(MatrixFactory.createColumnVector(0.25, 0.375, 0.375)), ERROR);
    assertEquals(6.0,
        d.getProb(MatrixFactory.createColumnVector(1.0, 0.0, 0.0)), ERROR);

    assertEquals(Double.NEGATIVE_INFINITY,
        d.getLogProb(MatrixFactory.createColumnVector(0.0, 0.5, 0.5)), ERROR);
    assertEquals(Math.log(1.5),
        d.getLogProb(MatrixFactory.createColumnVector(0.25, 0.375, 0.375)),
        ERROR);
    assertEquals(Math.log(6.0),
        d.getLogProb(MatrixFactory.createColumnVector(1.0, 0.0, 0.0)), ERROR);
  }

  /** Dirichlet, alpha = [2, 2, 2]. */
  public void testDirichlet3(Dirichlet d) {
    assertEquals(0, d.getProb(MatrixFactory.createColumnVector(0.4, 0.4, 0.4)),
        ERROR);
    assertEquals(0, d.getProb(MatrixFactory.createColumnVector(0.5, 0.5, 0.0)),
        ERROR);
    assertEquals(0, d.getProb(MatrixFactory.createColumnVector(0.0, 0.5, 0.5)),
        ERROR);
    assertEquals(3.75,
        d.getProb(MatrixFactory.createColumnVector(0.25, 0.25, 0.5)), ERROR);
    assertEquals(3.75,
        d.getProb(MatrixFactory.createColumnVector(0.5, 0.25, 0.25)), ERROR);

    assertEquals(Double.NEGATIVE_INFINITY,
        d.getLogProb(MatrixFactory.createColumnVector(0.4, 0.4, 0.4)), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY,
        d.getLogProb(MatrixFactory.createColumnVector(0.5, 0.5, 0.0)), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY,
        d.getLogProb(MatrixFactory.createColumnVector(0.0, 0.5, 0.5)), ERROR);
    assertEquals(Math.log(3.75),
        d.getLogProb(MatrixFactory.createColumnVector(0.25, 0.25, 0.5)), ERROR);
    assertEquals(Math.log(3.75),
        d.getLogProb(MatrixFactory.createColumnVector(0.5, 0.25, 0.25)), ERROR);
  }

  /** Dirichlet, alpha = [3, 3, 4]. */
  public void testDirichlet4(Dirichlet d) {
    assertEquals(0, d.getProb(MatrixFactory.createColumnVector(0.4, 0.4, 0.4)),
        ERROR);
    assertEquals(5.4432,
        d.getProb(MatrixFactory.createColumnVector(0.5, 0.3, 0.2)), ERROR);
    assertEquals(15.676416,
        d.getProb(MatrixFactory.createColumnVector(0.3, 0.3, 0.4)), ERROR);

    assertEquals(Double.NEGATIVE_INFINITY,
        d.getProb(MatrixFactory.createColumnVector(0.4, 0.4, 0.4)), ERROR);
    assertEquals(Math.log(5.4432),
        d.getProb(MatrixFactory.createColumnVector(0.5, 0.3, 0.2)), ERROR);
    assertEquals(Math.log(15.676416),
        d.getProb(MatrixFactory.createColumnVector(0.3, 0.3, 0.4)), ERROR);
  }

  @Test
  public void testProbabilityViaConstructor() {
    // no longer needed. will remove.
  }

  @Test
  public void testProbabilityViaSetParams() {
    Dirichlet d = new Dirichlet();
    d.setParams(new Object[] { MatrixFactory.createColumnVector(1, 1) });
    testDirichlet1(d);
    d.setParams(new Object[] { MatrixFactory.createColumnVector(2, 1, 1) });
    testDirichlet2(d);
    d.setParams(new Object[] { MatrixFactory.createColumnVector(2, 2, 2) });
    testDirichlet3(d);
    d.setParams(new Object[] { MatrixFactory.createColumnVector(3, 3, 4) });
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments() {
    Dirichlet d = new Dirichlet();
    d.setParams(new Object[] { null });
    Util.initRandom(false);
    d.sampleVal();
  }

  // Not a column vector
  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments() {
    Dirichlet d = new Dirichlet();
    double[][] ary = new double[1][2];
    ary[0][0] = 2.0;
    ary[0][1] = 2.0;
    MatrixLib l = MatrixFactory.fromArray(ary);
    d.setParams(new Object[] { l });
    Util.initRandom(false);
    d.sampleVal();
  }

  // All elements in the alpha vector must be strictly positive
  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments2() {
    Dirichlet d = new Dirichlet();
    d.setParams(MatrixFactory.createColumnVector(0, 0.5));
  }

  // All elements in the alpha vector must be strictly positive
  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments3() {
    Dirichlet d = new Dirichlet();
    d.setParams(MatrixFactory.createColumnVector(-1, 0.5));
  }

  // Must provide at least two elements for the alpha vector
  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments4() {
    Dirichlet d = new Dirichlet();
    d.setParams(MatrixFactory.createColumnVector(0.5));
  }

  @Test
  // Two elements in alpha vector, only one element in outcome vector
  public void testIncorrectArguments5() {
    Dirichlet d = new Dirichlet();
    d.setParams(MatrixFactory.createColumnVector(1.0, 1.0));
    try {
      d.getProb(MatrixFactory.createColumnVector(1.0));
      fail("the domain is underspecified");
    } catch (IllegalArgumentException ex) {
    }
    try {
      d.getLogProb(MatrixFactory.createColumnVector(1.0));
      fail("the domain is underspecified");
    } catch (IllegalArgumentException ex) {
    }
  }

  @Test
  // Two elements in alpha vector, three elements in outcome vector
  public void testIncorrectArguments6() {
    Dirichlet d = new Dirichlet();
    d.setParams(MatrixFactory.createColumnVector(1.0, 1.0));
    try {
      d.getProb(MatrixFactory.createColumnVector(0.5, 0.25, 0.25));
      fail("the domain is overspecified");
    } catch (IllegalArgumentException ex) {
    }
    try {
      d.getLogProb(MatrixFactory.createColumnVector(0.5, 0.25, 0.25));
      fail("the domain is overspecified");
    } catch (IllegalArgumentException ex) {
    }
  }

  @Test
  public void testDoubleSet() {
    Dirichlet d = new Dirichlet();
    d.setParams(new Object[] { null });
    d.setParams(new Object[] { MatrixFactory.createColumnVector(1.0, 1.0) });
    testDirichlet1(d);
    d.setParams(new Object[] { null });
    d.setParams(new Object[] { MatrixFactory.createColumnVector(2.0, 1.0, 1.0) });
    testDirichlet2(d);
  }

  @Test
  public void testSample() {
    Dirichlet d = new Dirichlet();
    d.setParams(new Object[] { MatrixFactory.createColumnVector(1.0, 1.0) });
    Util.initRandom(false);
    d.sampleVal();
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
    Dirichlet d = new Dirichlet();
    d.setParams(new Object[] { MatrixFactory.createColumnVector(1, 1) });
    assertTrue(Arrays.equals(null, d.getFiniteSupport()));
  }
}
