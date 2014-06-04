package test.blog.distrib;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Set;

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
  private final double ERROR = 10e-10;

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
    Double[] params = new Double[2];
    params[0] = MEAN;
    params[1] = VARIANCE;
    UnivarGaussian gaussian = new UnivarGaussian();
    gaussian.setParams(params);
    testGaussian(gaussian);
  }

  @Test
  public void testRandomMeanFixedVariance() {
    Double[] params = new Double[2];
    params[1] = VARIANCE;
    UnivarGaussian gaussian = new UnivarGaussian();
    gaussian.setParams(params);
    gaussian.setParams(MEAN, null);
    testGaussian(gaussian);
  }

  @Test
  public void testFixedMeanRandomVariance() {
    Double[] params = new Double[2];
    params[0] = MEAN;
    UnivarGaussian gaussian = new UnivarGaussian();
    gaussian.setParams(params);
    gaussian.setParams(null, VARIANCE);
    testGaussian(gaussian);
  }

  @Test
  public void testRandomMeanRandomVariance() {
    Double[] params = new Double[2];
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

  public void testGaussian(UnivarGaussian gaussian) {
    Set<Double> points = probVals.keySet();
    for (Double point : points) {
      assertEquals(probVals.get(point), gaussian.getProb(point), ERROR);
      assertEquals(Math.log(probVals.get(point)), gaussian.getLogProb(point),
          ERROR);
    }
  }

}
