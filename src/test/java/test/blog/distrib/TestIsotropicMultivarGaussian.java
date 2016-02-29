package test.blog.distrib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import blog.common.Util;
import blog.common.numerical.MatrixFactory;
import blog.common.numerical.MatrixLib;
import blog.distrib.IsotropicMultivarGaussian;

/**
 * Unit tests for IsotropicMultivarGaussian.
 */
public class TestIsotropicMultivarGaussian {
  private MatrixLib mean;
  private double covarScale;

  public TestIsotropicMultivarGaussian() {
    mean = MatrixFactory.fromArray(new double[][] { { 1.0 }, { 3.0 } });
    covarScale = 5.0;
  }

  @Test
  public void testLogProb() {
    IsotropicMultivarGaussian gauss = new IsotropicMultivarGaussian();
    gauss.setParams(mean, covarScale);
    MatrixLib x = MatrixFactory.fromArray(new double[][] { { 2.0 }, { -1.0 } });
    assertEquals(-5.147314978843446, gauss.getLogProb(x), 1e-10);
    assertEquals(0.0058149971748879745, gauss.getProb(x), 1e-10);
  }

  @Test
  public void testSample() {
    Util.initRandom(false);
    IsotropicMultivarGaussian gauss = new IsotropicMultivarGaussian();
    gauss.setParams(mean, covarScale);
    gauss.sampleVal();

    /*-
    // Output some samples that we can manually check against python.
    for (int i = 0; i < 10000; i++) {
      MatrixLib sample = gauss.sample_value();
      System.out.println("    [" + sample.elementAt(0, 0) + ", "
          + sample.elementAt(1, 0) + "],");
    }
     */
  }

  @Test
  public void testGetFiniteSupport() {
    IsotropicMultivarGaussian gauss = new IsotropicMultivarGaussian();
    gauss.setParams(mean, covarScale);
    assertTrue(Arrays.equals(null, gauss.getFiniteSupport()));
  }

  @Test
  public void testHighDimensions() {
    IsotropicMultivarGaussian gauss = new IsotropicMultivarGaussian();
    final int d = 400;
    gauss.setParams(MatrixFactory.ones(d), 5.0);
    assertEquals(-689.86299576868907,
        gauss.getLogProb(MatrixFactory.ones(d).timesScale(0.9)), 1e-10);
  }
}
