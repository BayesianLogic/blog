package test;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import blog.distrib.UnivarGaussian;

/**
 * Unit tests for Univariate Gaussian
 */
@RunWith(JUnit4.class)
public class TestUnivariateCase {
  private final double ERROR_BOUND = 1e-10;
  private HashMap<Double, Double> probVals = new HashMap<Double, Double>();
  private final double MEAN = 0.5;
  private final double VARIANCE = 2.25;

  @Test
  public void setUp() {
    // We have a normal random variable Z ~ N(0.5, 1.5)
    // Key is x, Value is pdf of Z at x
    HashMap<Double, Double> probVals = new HashMap<Double, Double>();
    probVals.put(0.0, 0.25158881846199549);
    probVals.put(0.3, 0.26360789392387846);
    probVals.put(0.9, 0.25667124973067595);
    probVals.put(2.8, 0.082088348017233054);
    probVals.put(3.8, 0.023649728564154305);
    probVals.put(6.0, 0.00032018043441388045);
  }

  @Test
  /**
   *    Tests the getProb(List args, Obj value) function and 
   *    the getLogProb(List args, Obj value) function
   */
  public void testGetProb() {

    // Case 1 -- Mean and Variance are fixed
    List constructParams = new LinkedList();
    constructParams.add(MEAN);
    constructParams.add(VARIANCE);
    UnivarGaussian univ = new UnivarGaussian(constructParams);

    List args = new LinkedList();
    for (Map.Entry<Double, Double> entry : probVals.entrySet()) {
      assertEquals(entry.getValue(), univ.getProb(args, entry.getKey()),
          ERROR_BOUND);
      assertEquals(Math.log(entry.getValue()),
          univ.getLogProb(args, entry.getKey()), ERROR_BOUND);
    }

    // Case 2 -- Mean is random, Variance is fixed
    constructParams = new LinkedList();
    constructParams.add(VARIANCE);
    univ = new UnivarGaussian(constructParams);

    args = new LinkedList();
    args.add(MEAN);
    for (Map.Entry<Double, Double> entry : probVals.entrySet()) {
      assertEquals(entry.getValue(), univ.getProb(args, entry.getKey()),
          ERROR_BOUND);
      assertEquals(Math.log(entry.getValue()),
          univ.getLogProb(args, entry.getKey()), ERROR_BOUND);
    }

    // Case 3 -- Mean is random, Variance is random
    constructParams = new LinkedList();
    univ = new UnivarGaussian(constructParams);

    args = new LinkedList();
    args.add(MEAN);
    args.add(VARIANCE);
    for (Map.Entry<Double, Double> entry : probVals.entrySet()) {
      assertEquals(entry.getValue(), univ.getProb(args, entry.getKey()),
          ERROR_BOUND);
      assertEquals(Math.log(entry.getValue()),
          univ.getLogProb(args, entry.getKey()), ERROR_BOUND);
    }
  }

}
