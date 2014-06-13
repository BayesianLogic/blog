/**
 * 
 */
package test.blog.distrib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import blog.common.numerical.MatrixFactory;
import blog.distrib.UniformVector;

/**
 * Unit Tests for UniformVector
 */
public class TestUniformVector implements TestDistributions {
  private final double ERROR = 10e-5;

  /** Uniform Vector [0, 1] by [0, 1]. */
  public void testUniformVector1(UniformVector unif) {
    double[] lower = unif.getLower();
    double[] upper = unif.getUpper();
    assertEquals(2, unif.getDimension(), ERROR);
    assertEquals(0.0, lower[0], ERROR);
    assertEquals(1.0, upper[0], ERROR);
    assertEquals(0.0, lower[1], ERROR);
    assertEquals(1.0, upper[1], ERROR);

    double[][] colVector = new double[][] { { 0 }, { 0 } };
    assertEquals(1.0, unif.getProb(MatrixFactory.fromArray(colVector)), ERROR);
    assertEquals(0.0, unif.getLogProb(MatrixFactory.fromArray(colVector)),
        ERROR);
    colVector = new double[][] { { 0.5 }, { 0.5 } };
    assertEquals(1.0, unif.getProb(MatrixFactory.fromArray(colVector)), ERROR);
    assertEquals(0.0, unif.getLogProb(MatrixFactory.fromArray(colVector)),
        ERROR);
    colVector = new double[][] { { 1.0 }, { 1.0 } };
    assertEquals(1.0, unif.getProb(MatrixFactory.fromArray(colVector)), ERROR);
    assertEquals(0.0, unif.getLogProb(MatrixFactory.fromArray(colVector)),
        ERROR);
    colVector = new double[][] { { 0.5 }, { 1.5 } };
    assertEquals(0.0, unif.getProb(MatrixFactory.fromArray(colVector)), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY,
        unif.getLogProb(MatrixFactory.fromArray(colVector)), ERROR);
    colVector = new double[][] { { 1.1 }, { 0.9 } };
    assertEquals(0.0, unif.getProb(MatrixFactory.fromArray(colVector)), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY,
        unif.getLogProb(MatrixFactory.fromArray(colVector)), ERROR);
  }

  /** Uniform Vector [-1, 3] by [2.6, 3.6] by [6, 11]. */
  public void testUniformVector2(UniformVector unif) {
    double[] lower = unif.getLower();
    double[] upper = unif.getUpper();
    assertEquals(3, unif.getDimension(), ERROR);
    assertEquals(-1.0, lower[0], ERROR);
    assertEquals(3.0, upper[0], ERROR);
    assertEquals(2.6, lower[1], ERROR);
    assertEquals(3.6, upper[1], ERROR);
    assertEquals(6.0, lower[2], ERROR);
    assertEquals(11.0, upper[2], ERROR);

    double[][] colVector = new double[][] { { 0 }, { 0 }, { 0 } };
    assertEquals(0.0, unif.getProb(MatrixFactory.fromArray(colVector)), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY,
        unif.getLogProb(MatrixFactory.fromArray(colVector)), ERROR);

    colVector = new double[][] { { -1.1 }, { 2.8 }, { 8 } };
    assertEquals(0.0, unif.getProb(MatrixFactory.fromArray(colVector)), ERROR);
    assertEquals(Double.NEGATIVE_INFINITY,
        unif.getLogProb(MatrixFactory.fromArray(colVector)), ERROR);

    colVector = new double[][] { { -1 }, { 3.6 }, { 6 } };
    assertEquals(0.05, unif.getProb(MatrixFactory.fromArray(colVector)), ERROR);
    assertEquals(Math.log(0.05),
        unif.getLogProb(MatrixFactory.fromArray(colVector)), ERROR);

    colVector = new double[][] { { 0.0 }, { 3.0 }, { 6.9 } };
    assertEquals(0.05, unif.getProb(MatrixFactory.fromArray(colVector)), ERROR);
    assertEquals(Math.log(0.05),
        unif.getLogProb(MatrixFactory.fromArray(colVector)), ERROR);
  }

  @Test
  public void testProbabilityViaConstructor() {
    // no longer needed. will be removed.
  }

  @Test
  public void testProbabilityViaSetParams() {
    UniformVector unif = new UniformVector();
    unif.setParams(new Object[] { MatrixFactory.createRowVector(0, 1),
        MatrixFactory.createRowVector(0, 1) });
    testUniformVector1(unif);
    unif = new UniformVector();
    unif.setParams(new Object[] { MatrixFactory.createRowVector(-1, 3),
        MatrixFactory.createRowVector(2.6, 3.6),
        MatrixFactory.createRowVector(6, 11) });
    testUniformVector2(unif);
  }

  // dimension is fixed upon first call to setParams
  @Test(expected = IllegalArgumentException.class)
  public void testFixedDimension() {
    UniformVector unif = new UniformVector();
    unif.setParams(new Object[] { MatrixFactory.createRowVector(0, 1),
        MatrixFactory.createRowVector(0, 1) });
    testUniformVector1(unif);
    unif.setParams(new Object[] { MatrixFactory.createRowVector(0, 1),
        MatrixFactory.createRowVector(0, 1),
        MatrixFactory.createRowVector(1, 3) });
    unif.sampleVal();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments() {
    UniformVector unif = new UniformVector();
    unif.setParams(new Object[] { null });
    unif.sampleVal(); // should throw error
  }

  // N by 3 vector does not make sense
  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments() {
    UniformVector unif = new UniformVector();
    double[][] box = new double[][] { { 0, 1, 2 }, { 3, 4, 5 } };
    unif.setParams(new Object[] { MatrixFactory.fromArray(box) });
    unif.sampleVal(); // either this line throw error or line above; doesn't
                      // strictly matter as long as sampleVal() fails to run
  }

  // N by 1 vector does not make sense
  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments2() {
    UniformVector unif = new UniformVector();
    double[][] box = new double[][] { { 0 }, { 4 }, { 6 }, { 9 } };
    unif.setParams(new Object[] { MatrixFactory.fromArray(box) });
    unif.sampleVal();
  }

  // min > max for one of the dimensions
  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments3() {
    UniformVector unif = new UniformVector();
    double[][] box = new double[][] { { 0, 3 }, { 4, 3 }, { 1, 2 } };
    unif.setParams(new Object[] { MatrixFactory.fromArray(box) });
    unif.sampleVal();
  }

  // not all row vectors specified
  @Test(expected = IllegalArgumentException.class)
  public void testIncompleteSet() {
    UniformVector unif = new UniformVector();
    unif.setParams(new Object[] { null, null, null });
    unif.setParams(new Object[] { MatrixFactory.createRowVector(1.0, 2.0),
        null, null });
    unif.setParams(new Object[] { MatrixFactory.createRowVector(1.0, 3.0),
        null, null });
    unif.setParams(new Object[] { null,
        MatrixFactory.createRowVector(1.0, 3.0), null });
    unif.sampleVal();
  }

  @Test
  public void testDoubleSet() {
    UniformVector unif = new UniformVector();
    unif.setParams(new Object[] { MatrixFactory.createRowVector(0, 1), null });
    unif.setParams(new Object[] { null, MatrixFactory.createRowVector(0, 2) });
    unif.setParams(new Object[] { null, MatrixFactory.createRowVector(0, 1) });
    testUniformVector1(unif);
  }

}
