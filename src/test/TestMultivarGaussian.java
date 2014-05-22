package test;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import blog.common.numerical.MatrixFactory;
import blog.common.numerical.MatrixLib;
import blog.distrib.MultivarGaussian;

/**
 * Unit tests for MultivarGaussian.
 */
@RunWith(JUnit4.class)
public class TestMultivarGaussian {
  private final double ERROR_BOUND = 1e-10;
  private MatrixLib mean;
  private MatrixLib variance;
  private HashMap<MatrixLib, Double> probVals;
  private LinkedList<MatrixLib> constructParams;
  private LinkedList<MatrixLib> args;

  public TestMultivarGaussian() {
    // Create a Gaussian with a mean and covariance matrix
    mean = MatrixFactory.zeros(2, 1);
    mean.setElement(0, 0, 1);
    mean.setElement(1, 0, 3);

    variance = MatrixFactory.eye(2);
    variance.setElement(0, 0, 4.25);
    variance.setElement(0, 1, 3.1);
    variance.setElement(1, 0, 3.1);
    variance.setElement(1, 1, 9.64);

    // Used Scipy to compute the pdf of this MVG at particular points
    probVals = new HashMap<MatrixLib, Double>();
    probVals.put(MatrixFactory.fromArray(new double[][] { { 0 }, { 0 } }),
        0.017816329802283811);
    probVals.put(MatrixFactory.fromArray(new double[][] { { 0.5 }, { 0.7 } }),
        0.021411078817775459);
    probVals.put(MatrixFactory.fromArray(new double[][] { { 1.0 }, { 3.0 } }),
        0.028420525552124164);
    probVals.put(MatrixFactory.fromArray(new double[][] { { 5.0 }, { 5.0 } }),
        0.0040865387874093571);

    constructParams = new LinkedList<MatrixLib>();
    args = new LinkedList<MatrixLib>();
  }

  /**
   * Case 1: Mean fixed, Variance fixed.
   */
  @Test
  public void case1() {
    constructParams.add(mean);
    constructParams.add(variance);
    testGaussian(constructParams, args);
  }

  /**
   * Case 2: Mean varies, Variance fixed.
   */
  @Test
  public void case2() {
    constructParams.add(variance);
    args.add(mean);
    testGaussian(constructParams, args);
  }

  /**
   * Case 3 -- Mean varies, Variance varies.
   */
  @Test
  public void case3() {
    args.add(mean);
    args.add(variance);
    testGaussian(constructParams, args);
  }

  /**
   * Error Case 1: Fixed variance and fixed mean. Extraneous random argument.
   */
  @Test(expected = IllegalArgumentException.class)
  public void errorCase1() {
    constructParams.add(mean);
    constructParams.add(variance);
    args.add(mean);
    testGaussian(constructParams, args);
  }

  /**
   * Error Case 2: No variance provided.
   */
  @Test(expected = IllegalArgumentException.class)
  public void errorCase2() {
    constructParams.add(mean);
    testGaussian(constructParams, args);
  }

  /**
   * Error Case 3: Variance provided as the first fixed parameter,
   * and mean provided as the second fixed parameter.
   */
  @Test(expected = IllegalArgumentException.class)
  public void errorCase3() {
    constructParams.add(variance);
    constructParams.add(mean);
    testGaussian(constructParams, args);
  }

  /**
   * Error Case 4: Mean provided as a fixed argument,
   * Covariance as a random argument.
   */
  @Test(expected = IllegalArgumentException.class)
  public void errorCase4() {
    constructParams.add(mean);
    args.add(variance);
    testGaussian(constructParams, args);
  }

  /**
   * Error Case 5: No fixed parameters, more than 2 random parameters.
   */
  @Test(expected = IllegalArgumentException.class)
  public void errorCase5() {
    args.add(mean);
    args.add(variance);
    args.add(mean);
    testGaussian(constructParams, args);
  }

  /**
   * Error Case 6: No fixed parameters, Only 1 random parameter.
   */
  @Test(expected = IllegalArgumentException.class)
  public void errorCase6() {
    args.add(mean);
    testGaussian(constructParams, args);
  }

  /**
   * Error Case 7: No fixed parameters. Covariance provided
   * as the first argument and the mean as the argument
   */
  @Test(expected = IllegalArgumentException.class)
  public void errorCase7() {
    args.add(variance);
    args.add(mean);
    testGaussian(constructParams, args);
  }

  public void testGaussian(List<MatrixLib> constructParams, List<MatrixLib> args) {
    MultivarGaussian mvg = new MultivarGaussian(constructParams);
    Set<MatrixLib> points = probVals.keySet();
    for (MatrixLib point : points) {
      assertEquals(probVals.get(point), mvg.getProb(args, point), ERROR_BOUND);
      assertEquals(Math.log(probVals.get(point)), mvg.getLogProb(args, point),
          ERROR_BOUND);
    }
  }

  /**
   * Regression test: Sigma with a tiny determinant does not cause problems.
   */
  @Test
  public void testSigmaTinyDeterminant() {
    MatrixLib mean = MatrixFactory.zeros(400, 1);
    MatrixLib sigma = MatrixFactory.eye(400).timesScale(0.001);

    // Determinant is too small to fit in double precision.
    assertEquals(sigma.det(), 0, 1e-10);

    // MultivarGaussian works nonetheless.
    // (The values here were NOT verified against an external implementation.)
    MultivarGaussian dist = new MultivarGaussian(mean, sigma);
    assertEquals(dist.getLogProb(MatrixFactory.zeros(400, 1)),
        1013.9756425145674, 1e-10);
    assertEquals(dist.getLogProb(MatrixFactory.ones(400, 1).timesScale(0.2)),
        -6986.024357485432, 1e-10);
  }
}
