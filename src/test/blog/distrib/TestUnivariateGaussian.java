package test.blog.distrib;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import blog.distrib.UnivarGaussian;

/**
 * Unit tests for Univariate Gaussian
 */
@RunWith(JUnit4.class)
public class TestUnivariateGaussian {
  private HashMap<Double, Double> probVals;
  private final double MEAN = 0.5;
  private final double VARIANCE = 2.25;
  private final double ERROR = 1e-9;

  /** Univariate Gaussian, MEAN = 0.5, VARIANCe = 2.25. */
  public void testGaussian(UnivarGaussian gaussian) {
    assertEquals(0.25158881846199549, gaussian.getProb(0.0), ERROR);
    assertEquals(0.26360789392387846, gaussian.getProb(0.3), ERROR);
    assertEquals(0.25667124973067595, gaussian.getProb(0.9), ERROR);
    assertEquals(0.082088348017233054, gaussian.getProb(2.8), ERROR);
    assertEquals(0.023649728564154305, gaussian.getProb(3.8), ERROR);
    assertEquals(0.00032018043441388045, gaussian.getProb(6.0), ERROR);

    assertEquals(Math.log(0.25158881846199549), gaussian.getLogProb(0.0), ERROR);
    assertEquals(Math.log(0.26360789392387846), gaussian.getLogProb(0.3), ERROR);
    assertEquals(Math.log(0.25667124973067595), gaussian.getLogProb(0.9), ERROR);
    assertEquals(Math.log(0.082088348017233054), gaussian.getLogProb(2.8),
        ERROR);
    assertEquals(Math.log(0.023649728564154305), gaussian.getLogProb(3.8),
        ERROR);
    assertEquals(Math.log(0.00032018043441388045), gaussian.getLogProb(6.0),
        ERROR);
  }

  /** Univariate Gaussian, MEAN = 0.0, VARIANCE = 1.0. */
  public void testGaussian2(UnivarGaussian gaussian) {
    assertEquals(0.24197072451914337, gaussian.getProb(-1.0), ERROR);
    assertEquals(0.35206532676429952, gaussian.getProb(-0.5), ERROR);
    assertEquals(0.3989422804014327, gaussian.getProb(0), ERROR);
    assertEquals(Math.log(0.24197072451914337), gaussian.getLogProb(-1.0),
        ERROR);
    assertEquals(Math.log(0.35206532676429952), gaussian.getLogProb(-0.5),
        ERROR);
    assertEquals(Math.log(0.3989422804014327), gaussian.getLogProb(0), ERROR);
  }

  public TestUnivariateGaussian() {
    // We have a normal random variable Z ~ N(0.5, 1.5)
    probVals = new HashMap<Double, Double>();
    probVals.put(0.0, 0.25158881846199549);
    probVals.put(0.3, 0.26360789392387846);
    probVals.put(0.9, 0.25667124973067595);
    probVals.put(2.8, 0.082088348017233054);
    probVals.put(3.8, 0.023649728564154305);
    probVals.put(6.0, 0.00032018043441388045);
  }

  @Test
  public void testFixedMeanFixedVariance() {
    Double[] params = { MEAN, VARIANCE };
    UnivarGaussian gaussian = new UnivarGaussian();
    gaussian.setParams(params);
    testGaussian(gaussian);
  }

  @Test
  public void testRandomMeanFixedVariance() {
    Double[] params = { null, VARIANCE };
    UnivarGaussian gaussian = new UnivarGaussian();
    gaussian.setParams(params);
    gaussian.setParams(MEAN, null);
    testGaussian(gaussian);
  }

  @Test
  public void testFixedMeanRandomVariance() {
    Double[] params = { MEAN, null };
    UnivarGaussian gaussian = new UnivarGaussian();
    gaussian.setParams(params);
    gaussian.setParams(null, VARIANCE);
    testGaussian(gaussian);
  }

  @Test
  public void testRandomMeanRandomVariance() {
    Double[] params = { null, null };
    UnivarGaussian gaussian = new UnivarGaussian();
    gaussian.setParams(params);
    gaussian.setParams(MEAN, VARIANCE);
    testGaussian(gaussian);
  }

  @Test
  public void testDoubleSet() {
    UnivarGaussian gaussian = new UnivarGaussian();
    gaussian.setParams(MEAN, null);
    gaussian.setParams(null, VARIANCE);
    testGaussian(gaussian);
  }

  @Test
  public void testSetParamsIntegerArguments() {
    // Tests that providing integer arguments to mean and variance works
    // correctly.
    UnivarGaussian gaussian = new UnivarGaussian();
    gaussian.setParams(0, 1);
    testGaussian2(gaussian);
  }

  public void testGetProbIntegerArguments() {
    // Tests providing integer arguments to getProb
    UnivarGaussian gaussian = new UnivarGaussian();
    gaussian.setParams(0, 1);
    assertEquals(0.3989422804014327, gaussian.getProb(0), ERROR);
    assertEquals(Math.log(0.3989422804014327), gaussian.getLogProb(0), ERROR);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInSufficientArguments1() {
    UnivarGaussian gaussian = new UnivarGaussian();
    gaussian.setParams(null, VARIANCE);
    testGaussian(gaussian);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInSufficientArguments2() {
    UnivarGaussian gaussian = new UnivarGaussian();
    gaussian.setParams(MEAN, null);
    testGaussian(gaussian);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIncorrectNumberArguments() {
    Double[] params = new Double[1];
    UnivarGaussian gaussian = new UnivarGaussian();
    gaussian.setParams(params);
    testGaussian(gaussian);
  }

}
