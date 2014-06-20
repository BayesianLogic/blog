package test.blog.distrib;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import blog.common.Util;
import blog.common.numerical.MatrixFactory;
import blog.common.numerical.MatrixLib;
import blog.distrib.MultivarGaussian;

/**
 * Unit tests for MultivarGaussian.
 */
@RunWith(JUnit4.class)
public class TestMultivarGaussian implements TestDistributions {
  private MatrixLib mean;
  private MatrixLib variance;
  private HashMap<MatrixLib, Double> probVals;
  private final double ERROR = 10e-5;

  public TestMultivarGaussian() {
    // Create a Gaussian with a mean and covariance matrix
    mean = MatrixFactory.zeros(1, 2);
    mean.setElement(0, 0, 1);
    mean.setElement(0, 1, 3);

    variance = MatrixFactory.eye(2);
    variance.setElement(0, 0, 4.25);
    variance.setElement(0, 1, 3.1);
    variance.setElement(1, 0, 3.1);
    variance.setElement(1, 1, 9.64);

    // Used Scipy to compute the pdf of this MVG at particular points
    probVals = new HashMap<MatrixLib, Double>();
    probVals.put(MatrixFactory.fromArray(new double[][] { { 0, 0 } }),
        0.017816329802283811);
    probVals.put(MatrixFactory.fromArray(new double[][] { { 0.5, 0.7 } }),
        0.021411078817775459);
    probVals.put(MatrixFactory.fromArray(new double[][] { { 1.0, 3.0 } }),
        0.028420525552124164);
    probVals.put(MatrixFactory.fromArray(new double[][] { { 5.0, 5.0 } }),
        0.0040865387874093571);

  }

  public void testGaussian(MultivarGaussian gauss) {
    Set<MatrixLib> points = probVals.keySet();
    for (MatrixLib point : points) {
      assertEquals(probVals.get(point), gauss.getProb(point), ERROR);
      assertEquals(Math.log(probVals.get(point)), gauss.getLogProb(point),
          ERROR);
    }
  }

  /**
   * Regression test: Sigma with a tiny determinant does not cause problems.
   */
  @Test
  public void testSigmaTinyDeterminant() {
    MatrixLib mean = MatrixFactory.zeros(1, 400);
    MatrixLib sigma = MatrixFactory.eye(400).timesScale(0.001);

    // Determinant is too small to fit in double precision.
    assertEquals(sigma.det(), 0, 1e-10);

    // MultivarGaussian works nonetheless.
    // (The values here were NOT verified against an external implementation.)
    MultivarGaussian dist = new MultivarGaussian();
    dist.setParams(mean, sigma);
    assertEquals(dist.getLogProb(MatrixFactory.zeros(1, 400)),
        1013.9756425145674, 1e-10);
    assertEquals(dist.getLogProb(MatrixFactory.ones(1, 400).timesScale(0.2)),
        -6986.024357485432, 1e-10);
  }

  @Test
  public void testProbabilityViaConstructor() {
    // no longer needed. will be removed.
  }

  @Test
  public void testProbabilityViaSetParams() {
    MultivarGaussian gauss = new MultivarGaussian();
    gauss.setParams(mean, variance);
    testGaussian(gauss);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments() {
    MultivarGaussian gauss = new MultivarGaussian();
    gauss.setParams(mean, null);
    gauss.sampleVal();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments2() {
    MultivarGaussian gauss = new MultivarGaussian();
    gauss.setParams(null, variance);
    gauss.sampleVal();
  }

  // covariance matrix not symmetric
  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments() {
    MultivarGaussian gauss = new MultivarGaussian();
    MatrixLib matrix = MatrixFactory.zeros(2, 2);
    matrix.setElement(0, 1, 1.5);
    matrix.setElement(1, 0, 1.7);
    gauss.setParams(mean, matrix);
    gauss.sampleVal();
  }

  // mean vector and covariance matrix of different dimensions
  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments2() {
    MultivarGaussian gauss = new MultivarGaussian();
    MatrixLib sigma = MatrixFactory.eye(2);
    double[][] ary = new double[3][1];
    MatrixLib mean = MatrixFactory.fromArray(ary);
    gauss.setParams(mean, sigma);
    gauss.sampleVal();
  }

  // covariance matrix is not square
  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectArguments3() {
    MultivarGaussian gauss = new MultivarGaussian();
    MatrixLib sigma = MatrixFactory.zeros(2, 3);
    double[][] ary = new double[3][1];
    MatrixLib mean = MatrixFactory.fromArray(ary);
    gauss.setParams(mean, sigma);
    gauss.sampleVal();
  }

  @Test
  public void testDoubleSet() {
    // TODO Auto-generated method stub

  }

  @Test
  public void testSample() {
    Util.initRandom(false);
    MultivarGaussian gauss = new MultivarGaussian();
    gauss.setParams(mean, variance);
    gauss.sampleVal();
  }
}
