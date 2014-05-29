package test;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
  private final double ERROR_BOUND = 1e-10;
  private HashMap<Double, Double> probVals;
  private final double MEAN = 0.5;
  private final double VARIANCE = 2.25;
  private List<Object> constructParams;
  private List<Object> args;

  public TestUnivariateGaussian() {
    // We have a normal random variable Z ~ N(0.5, 1.5)
    probVals = new HashMap<Double, Double>();
    probVals.put(0.0, 0.25158881846199549);
    probVals.put(0.3, 0.26360789392387846);
    probVals.put(0.9, 0.25667124973067595);
    probVals.put(2.8, 0.082088348017233054);
    probVals.put(3.8, 0.023649728564154305);
    probVals.put(6.0, 0.00032018043441388045);

    constructParams = new LinkedList<Object>();
    args = new LinkedList<Object>();
  }

  @Test
  public void testFixedMeanFixedVariance() {
    constructParams.add(MEAN);
    constructParams.add(VARIANCE);
    testGaussian(constructParams, args);
  }

  @Test
  public void testRandomMeanFixedVariance() {
    constructParams.add(VARIANCE);
    args.add(MEAN);
    testGaussian(constructParams, args);
  }

  @Test
  public void testRandomMeanRandomVariance() {
    args.add(MEAN);
    args.add(VARIANCE);
    testGaussian(constructParams, args);
  }

  public void testGaussian(List<Object> constructParams, List<Object> args) {
    UnivarGaussian gaussian = new UnivarGaussian();
    gaussian.setParams(constructParams);
    Set<Double> points = probVals.keySet();
    for (Double point : points) {
      gaussian.setParams(args);
      assertEquals(probVals.get(point), gaussian.getProb(point), ERROR_BOUND);
      assertEquals(Math.log(probVals.get(point)), gaussian.getLogProb(point),
          ERROR_BOUND);
    }
  }

}
