/**
 * 
 */
package test.blog.distrib;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Set;

import org.junit.Test;

import blog.common.Util;
import blog.common.numerical.MatrixFactory;
import blog.common.numerical.MatrixLib;
import blog.distrib.InverseWishart;

public class TestInverseWishart {

  private MatrixLib scale;
  private int freeDeg;
  private HashMap<MatrixLib, Double> probVals;
  private final double ERROR = 10e-5;

  public TestInverseWishart() {
    // Create a InverseWishart with a scale matrix and a degree of freedom
    scale = MatrixFactory.zeros(2, 2);
    scale.setElement(0, 0, 1);
    scale.setElement(1, 0, 0.5);
    scale.setElement(0, 1, 0.5);
    scale.setElement(1, 1, 2);

    freeDeg = 10;

    // Used Scipy to compute the pdf of this IW at particular points
    probVals = new HashMap<MatrixLib, Double>();
    probVals.put(
        MatrixFactory.fromArray(new double[][] { { 1, 2 }, { 2, 5 } }),
        0.0000026590325348348356);
    probVals.put(
        MatrixFactory.fromArray(new double[][] { { 1, 0 }, { 0, 1 } }),
        0.0000072279998207229272);
  }

  public void testInverseWishart(InverseWishart iwish) {
    Set<MatrixLib> points = probVals.keySet();
    for (MatrixLib point : points) {
      assertEquals(probVals.get(point), iwish.getProb(point), ERROR);
      assertEquals(Math.log(probVals.get(point)), iwish.getLogProb(point),
          ERROR);
    }
  }

  /*
   * PDF test: scale = [1, 0.5; 0.5, 2], freeDeg = 10;
   */
  @Test
  public void testPDF1() {
    InverseWishart iwish = new InverseWishart();
    iwish.setParams(scale, freeDeg);
    testInverseWishart(iwish);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments() {
    InverseWishart iwish = new InverseWishart();
    iwish.setParams(scale, null);
    iwish.sampleVal();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments2() {
    InverseWishart iwish = new InverseWishart();
    iwish.setParams(null, freeDeg);
    iwish.sampleVal();
  }

  @Test
  public void testSample() {
    Util.initRandom(false);
    InverseWishart iwish = new InverseWishart();
    iwish.setParams(scale, freeDeg);
    System.out.println(iwish.sample_value().timesScale(freeDeg - 2 - 1)
        .toString());
    freeDeg = 1000;
    iwish.setParams(scale, freeDeg);
    System.out.println(iwish.sample_value().timesScale(freeDeg - 2 - 1)
        .toString());
  }
}
