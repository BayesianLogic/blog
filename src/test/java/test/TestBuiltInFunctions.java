package test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import blog.common.numerical.MatrixFactory;
import blog.common.numerical.MatrixLib;
import blog.model.BuiltInFunctions;

/**
 * Unit tests for BuiltInFunctions.
 */
@RunWith(JUnit4.class)
public class TestBuiltInFunctions {

  @Test
  public void testIndexRowVector() {
    MatrixLib rowVec = MatrixFactory.zeros(1, 5);
    Object[] args = { rowVec, 3 };
    double result = (Double) BuiltInFunctions.SUB_REAL_ARRAY.getValue(args);
    assertEquals(result, 0.0, 1e-10);
  }

  @Test
  public void testIndexColumnVector() {
    MatrixLib colVec = MatrixFactory.zeros(5, 1);
    Object[] args = { colVec, 3 };
    double result = (Double) BuiltInFunctions.SUB_REAL_ARRAY.getValue(args);
    assertEquals(result, 0.0, 1e-10);
  }
}
