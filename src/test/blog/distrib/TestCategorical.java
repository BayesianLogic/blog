/**
 * 
 */
package test.blog.distrib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.Test;

import blog.common.numerical.MatrixFactory;
import blog.common.numerical.MatrixLib;
import blog.distrib.Categorical;

/**
 * Unit Tests for Categorical Distribution
 */
public class TestCategorical extends TestDistribution {

  @Test
  public void testArrayListImplementation() {
    // Fixed Case
    ArrayList<Double> probs = new ArrayList<Double>();
    probs.add(0.5);
    probs.add(0.3);
    probs.add(0.2);
    constructParams.add(probs);
    Categorical categorical = new Categorical(constructParams);
    assertEquals(0.5, categorical.getProb(null, 0), ERROR_BOUND);
    assertEquals(0.3, categorical.getProb(null, 1), ERROR_BOUND);
    assertEquals(0.2, categorical.getProb(null, 2), ERROR_BOUND);
    assertEquals(0, categorical.getProb(null, 3), ERROR_BOUND);

    // Random Case (currently unsupported)
    constructParams.clear();
    categorical = new Categorical(constructParams);
    fail("random case is currently unsupported");
  }

  @Test
  public void testMatrixLibImplementation() {
    // Fixed Case
    double[][] array = new double[1][3];
    array[0][0] = 0.2;
    array[0][1] = 0.3;
    array[0][2] = 0.5;
    MatrixLib lib = MatrixFactory.fromArray(array);
    constructParams.add(lib);
    Categorical categorical = new Categorical(constructParams);
    assertEquals(0.2, categorical.getProb(null, 0), ERROR_BOUND);
    assertEquals(0.3, categorical.getProb(null, 1), ERROR_BOUND);
    assertEquals(0.5, categorical.getProb(null, 2), ERROR_BOUND);
    assertEquals(0.0, categorical.getProb(null, 3), ERROR_BOUND);

    // Random Case
    constructParams.clear();
    categorical = new Categorical(constructParams);
    args.add(lib);
    assertEquals(0.2, categorical.getProb(args, 0), ERROR_BOUND);
    assertEquals(0.3, categorical.getProb(args, 1), ERROR_BOUND);
    assertEquals(0.5, categorical.getProb(args, 2), ERROR_BOUND);
    assertEquals(0.0, categorical.getProb(args, 3), ERROR_BOUND);
  }

  @Test
  public void testMapImplementation() {
    fail("Not tested yet. Waiting for new interface.");
  }

}
