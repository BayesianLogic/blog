package test;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

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
public class TestMultivarGaussian {

  @Test
  public void setUp() {

  }

  @Test
  public void testSample() {
    Util.initRandom(true);

    MatrixLib mean = MatrixFactory.zeros(5, 1);
    MatrixLib sigma = MatrixFactory.eye(5);
    MultivarGaussian mvg = new MultivarGaussian(mean, sigma);

    MatrixLib b = mvg.sampleVal();
    // System.out.println(b.toString());
  }

  @Test
  public void testGetProb() {
    // Case 1 -- Mean and Variance are fixed
    List constructParams = new LinkedList();
    MatrixLib mean = MatrixFactory.zeros(2, 1);
    mean.setElement(1, 0, 1);
    mean.setElement(0, 0, 3);

    MatrixLib variance = MatrixFactory.eye(2);
    variance.setElement(0, 0, 4.25);
    variance.setElement(0, 1, 3.1);
    variance.setElement(1, 0, 3.1);
    variance.setElement(1, 1, 9.64);

    List params = new LinkedList();
    params.add(mean);
    params.add(variance);
    MultivarGaussian mvg = new MultivarGaussian(params);

    // Case 2 -- Mean varies, Variance fixed

    // Case 3 -- Mean varies, Variance varies
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
