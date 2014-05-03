package test;

import Jama.Matrix;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.junit.Assert.assertEquals;

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
  public void testSample() {
    Util.initRandom(true);

    MatrixLib mean = MatrixFactory.zeros(5, 1);
    MatrixLib sigma = MatrixFactory.eye(5);
    MultivarGaussian mvg = new MultivarGaussian(mean, sigma);

    MatrixLib b = mvg.sampleVal();
    // System.out.println(b.toString());
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
    assertEquals(
      dist.getLogProb(MatrixFactory.zeros(400, 1)),
      1013.9756425145674,
      1e-10);
    assertEquals(
      dist.getLogProb(MatrixFactory.ones(400, 1).timesScale(0.2)),
      -6986.024357485432,
      1e-10);
  }
}
