package test.blog.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import blog.common.numerical.MatrixFactory;
import blog.common.numerical.MatrixLib;
import blog.model.BuiltInFunctions;
import blog.model.Model;
import blog.sample.DefaultEvalContext;
import blog.world.PartialWorld;

/**
 * Unit tests for BuiltInFunctions.
 */
public class TestBuiltInFunctions {

  @Test
  public void testIndexRowVector() {
    MatrixLib rowVec = MatrixFactory.zeros(1, 5);
    Object[] args = { rowVec, 3 };
    double result = (Double) BuiltInFunctions.SUB_MAT.getValueInContext(args,
        new DefaultEvalContext(PartialWorld.EMPTY_INST), false);
    assertEquals(result, 0.0, 1e-10);
  }

  @Test
  public void testIndexColumnVector() {
    MatrixLib colVec = MatrixFactory.zeros(5, 1);
    Object[] args = { colVec, 3 };
    double result = (Double) BuiltInFunctions.SUB_MAT.getValueInContext(args,
        new DefaultEvalContext(PartialWorld.EMPTY_INST), false);
    assertEquals(result, 0.0, 1e-10);
  }

  @Test
  public void testMinMax() {
    Model.fromFile("src/test/models/minmax.blog");
  }
}
