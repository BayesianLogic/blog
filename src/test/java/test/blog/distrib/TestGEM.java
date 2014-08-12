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
import blog.distrib.GEM;

public class TestGEM {

  private double lambda;
  private int truncation, truncation2;
  private HashMap<MatrixLib, Double> probVals;
  private final double ERROR = 10e-5;

  public TestGEM() {
    // Create a GEM with a parameter lambda and a stop point parameter
    lambda = 2.0;
    truncation = 3;
    truncation2 = 100;

    // Compute the pdf of this GEM at particular points
    probVals = new HashMap<MatrixLib, Double>();
    probVals.put(
        MatrixFactory.fromArray(new double[][] { { 0.4 }, { 0.3 }, { 0.3 } }),
        2.0);
    probVals.put(
        MatrixFactory.fromArray(new double[][] { { 0.5 }, { 0.2 }, { 0.3 } }),
        2.4);
  }

  public void testGEM(GEM gem) {
    Set<MatrixLib> points = probVals.keySet();
    for (MatrixLib point : points) {
      assertEquals(probVals.get(point), gem.getProb(point), ERROR);
      assertEquals(Math.log(probVals.get(point)), gem.getLogProb(point), ERROR);
    }
  }

  /*
   * PDF test: lambda = 2.0, truncation = 3;
   */
  @Test
  public void testPDF1() {
    GEM gem = new GEM();
    gem.setParams(lambda, truncation);
    testGEM(gem);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments() {
    GEM gem = new GEM();
    gem.setParams(lambda, null);
    gem.sampleVal();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientArguments2() {
    GEM gem = new GEM();
    gem.setParams(null, truncation);
    gem.sampleVal();
  }

  /*
   * Generate 10 samples with lambda = 2.0 and truncation = 100.
   */
  @Test
  public void testSample() {
    Util.initRandom(false);
    GEM gem = new GEM();
    gem.setParams(lambda, truncation2);
    for (int i = 0; i < 10; i++)
      System.out.println(gem.sample_value());
  }
}
