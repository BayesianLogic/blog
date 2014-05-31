/**
 * 
 */
package test;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import blog.common.numerical.MatrixFactory;
import blog.common.numerical.MatrixLib;
import blog.distrib.Dirichlet;

/**
 * Unit Tests for Dirichlet
 */
public class TestDirichlet extends TestDistribution {

  @Test
  public void testGetProb() {
    testDistribution(false);
  }

  @Test
  public void testGetLogProb() {
    testDistribution(true);
  }

  /**
   * @param testLogProb
   *          -- Boolean value indicating that is true when the method tests
   *          getLogProb and is false when the method tests getProb. This is
   *          implemented so as to retain the division of unit test between
   *          getProb and getLogProb for readability purposes.
   * 
   */
  public void testDistribution(boolean testLogProb) {
    // Test Dirichlet(1, 1) returns pdf of 1 for all support vectors
    List<Double> uniform = new LinkedList<Double>();
    uniform.add(1.0);
    uniform.add(1.0);
    for (int i = 0; i <= 4; i++) {
      List<Double> probs = new LinkedList<Double>();
      probs.add((i + 0.0) / 4);
      probs.add(1.0 - (i + 0.0) / 4);
      testDirichletDistribution(uniform, probs, 1.0, testLogProb);
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
      testDirichletDistribution(params, probs, 6 * probA, testLogProb);
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
    testDirichletDistribution(params, probs, 0, testLogProb);
    probs.clear();
    probs.add(0.5);
    probs.add(0.0);
    probs.add(0.5);
    testDirichletDistribution(params, probs, 0, testLogProb);
    probs.clear();
    probs.add(0.25);
    probs.add(0.25);
    probs.add(0.5);
    testDirichletDistribution(params, probs, 3.75, testLogProb);
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
   * @param isLog
   *          -- Boolean value indicating that is true when the method tests
   *          getLogProb and is false when the method tests getProb. This is
   *          implemented so as to retain the division of unit test between
   *          getProb and getLogProb for readability purposes.
   */
  public void testDirichletDistribution(List<Double> parameters,
      List<Double> probs, double pdf, boolean isLog) {
    Dirichlet d = new Dirichlet(parameters);
    double[][] values = new double[1][probs.size()];
    for (int i = 0; i < probs.size(); i++) {
      values[0][i] = probs.get(i);
    }
    MatrixLib probValues = MatrixFactory.fromArray(values);
    List<Object> args = new LinkedList<Object>();
    if (isLog) {
      assertEquals(Math.log(pdf), d.getLogProb(args, probValues), ERROR_BOUND);
    } else {
      assertEquals(pdf, d.getProb(args, probValues), ERROR_BOUND);
    }
  }
}
