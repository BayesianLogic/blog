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

  @Test
  public void testGetProb() {
    // Assign the mean and variance
    mean = MatrixFactory.zeros(2, 1);
    mean.setElement(0, 0, 1);
    mean.setElement(1, 0, 3);

    variance = MatrixFactory.eye(2);
    variance.setElement(0, 0, 4.25);
    variance.setElement(0, 1, 3.1);
    variance.setElement(1, 0, 3.1);
    variance.setElement(1, 1, 9.64);

    probVals = new HashMap<MatrixLib, Double>();
    probVals.put(MatrixFactory.fromArray(new double[][] { { 0 }, { 0 } }),
        0.017816329802283811);
    probVals.put(MatrixFactory.fromArray(new double[][] { { 0.5 }, { 0.7 } }),
        0.021411078817775459);
    probVals.put(MatrixFactory.fromArray(new double[][] { { 1.0 }, { 3.0 } }),
        0.028420525552124164);
    probVals.put(MatrixFactory.fromArray(new double[][] { { 5.0 }, { 5.0 } }),
        0.0040865387874093571);

    // Case 1 -- Mean and Variance are fixed
    List constructParams = new LinkedList();
    List args = new LinkedList();
    constructParams.add(mean);
    constructParams.add(variance);
    MultivarGaussian mvg = new MultivarGaussian(constructParams);
    testGaussian(mvg, args);

    // Case 2 -- Mean varies, Variance fixed
    constructParams = new LinkedList();
    constructParams.add(variance);
    args = new LinkedList();
    args.add(mean);
    mvg = new MultivarGaussian(constructParams);
    testGaussian(mvg, args);

    // Case 3 -- Mean varies, Variance varies
  }

  public void testGaussian(MultivarGaussian mvg, List args) {
    Set<MatrixLib> points = probVals.keySet();
    for (MatrixLib point : points) {
      assertEquals(probVals.get(point), mvg.getProb(args, point), ERROR_BOUND);
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
