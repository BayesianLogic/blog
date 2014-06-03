/**
 * 
 */
package test.blog.distrib;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import blog.common.Util;
import blog.common.numerical.MatrixFactory;
import blog.common.numerical.MatrixLib;
import blog.distrib.Dirichlet;
import blog.model.BuiltInTypes;

/**
 * Unit Tests for Dirichlet
 */
public class TestDirichlet extends TestDistribution {

  @Test
  public void testDistribution() {
    // Test Dirichlet(1, 1) returns pdf of 1 for all support vectors
    List<Double> uniform = new LinkedList<Double>();
    uniform.add(1.0);
    uniform.add(1.0);
    for (int i = 0; i <= 4; i++) {
      List<Double> probs = new LinkedList<Double>();
      probs.add((i + 0.0) / 4);
      probs.add(1.0 - (i + 0.0) / 4);
      testDirichletDistribution(uniform, probs, 1.0);
    }

    // Test Dirichlet(2, 1, 1) returns a linear pdf when scaled along parameter
    List<Double> params = new LinkedList<Double>();
    params.add(2.0);
    params.add(1.0);
    params.add(1.0);
    for (int i = 0; i <= 4; i++) {
      double probA = i * 0.25;
      List<Double> probs = new LinkedList<Double>();
      probs.add(probA);
      probs.add(1 - (probA / 2));
      probs.add(1 - (probA / 2));
      testDirichletDistribution(params, probs, 6 * probA);
    }

    // Test Dirichlet(2, 2, 2)
    params = new LinkedList<Double>();
    params.add(2.0);
    params.add(2.0);
    params.add(2.0);
    List<Double> probs = new LinkedList<Double>();
    probs.add(0.0);
    probs.add(0.5);
    probs.add(0.5);
    testDirichletDistribution(params, probs, 0);
    probs.clear();
    probs.add(0.5);
    probs.add(0.0);
    probs.add(0.5);
    testDirichletDistribution(params, probs, 0);
    probs.clear();
    probs.add(0.25);
    probs.add(0.25);
    probs.add(0.5);
    testDirichletDistribution(params, probs, 3.75);
  }

  /**
   * Refer to Wikipedia for Description of Dirichlet Distribution
   * 
   * @param parameters
   *          -- Concentration parameters (a_1, ... a_K)
   * @param probs
   *          -- (x_1, ... , x_K)
   * @param pdf
   *          -- The probability of the Dirichlet taking on (x_1,..., x_K) given
   *          the concentration parameters
   */
  public void testDirichletDistribution(List<Double> parameters,
      List<Double> probs, double pdf) {
    Dirichlet d = new Dirichlet(parameters);
    double[][] values = new double[1][probs.size()];
    for (int i = 0; i < probs.size(); i++) {
      values[0][i] = probs.get(i);
    }
    MatrixLib probValues = MatrixFactory.fromArray(values);
    List<Object> args = new LinkedList<Object>();
    assertEquals(Math.log(pdf), d.getLogProb(args, probValues), ERROR_BOUND);
    assertEquals(pdf, d.getProb(args, probValues), ERROR_BOUND);
  }

  @Test
  public void testSampleVal() {
    Util.initRandom(true);
    Dirichlet d = new Dirichlet(5, 1.0);
    MatrixLib samples = (MatrixLib) d.sampleVal(new ArrayList<Object>(),
        BuiltInTypes.NULL);

    double sum = 0.0;
    for (int i = 0; i < samples.numCols(); i++) {
      sum += samples.elementAt(0, i);
    }
    assertEquals(sum, 1.0, ERROR_BOUND);
  }
}
