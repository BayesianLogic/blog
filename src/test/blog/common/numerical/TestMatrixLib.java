/**
 * 
 */
package test.blog.common.numerical;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import blog.common.numerical.MatrixFactory;
import blog.common.numerical.MatrixLib;

/**
 * Unit Tests for MatrixLib methods
 */
public class TestMatrixLib {
  private static final double ERROR = 0.01;

  @Test
  public void testRepMat() {
    MatrixLib m = MatrixFactory.zeros(1, 1);
    m.setElement(0, 0, 5);
    MatrixLib n = m.repmat(3, 3);
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        assertEquals(5, n.elementAt(i, j), ERROR);
      }
    }
    assertEquals(3, n.numRows(), ERROR);
    assertEquals(3, n.numCols(), ERROR);

    m = MatrixFactory.zeros(1, 3);
    m.setElement(0, 0, 1);
    m.setElement(0, 1, 3);
    m.setElement(0, 2, 5);
    n = m.repmat(3, 2);
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 2; j++) {
        assertEquals(1, n.elementAt(i, j * 3 + 0), ERROR);
        assertEquals(3, n.elementAt(i, j * 3 + 1), ERROR);
        assertEquals(5, n.elementAt(i, j * 3 + 2), ERROR);
      }
    }
    assertEquals(3, n.numRows(), ERROR);
    assertEquals(6, n.numCols(), ERROR);

    m = MatrixFactory.zeros(3, 2);
    m.setElement(0, 0, 1);
    m.setElement(1, 1, 4);
    m.setElement(2, 0, 5);
    n = m.repmat(4, 2);
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 2; j++) {
        assertEquals(1, n.elementAt(i * 3, j * 2), ERROR);
        assertEquals(0, n.elementAt(i * 3, j * 2 + 1), ERROR);
        assertEquals(0, n.elementAt(i * 3 + 1, j * 2), ERROR);
        assertEquals(4, n.elementAt(i * 3 + 1, j * 2 + 1), ERROR);
        assertEquals(5, n.elementAt(i * 3 + 2, j * 2), ERROR);
        assertEquals(0, n.elementAt(i * 3 + 2, j * 2 + 1), ERROR);
      }
    }
    assertEquals(12, n.numRows(), ERROR);
    assertEquals(4, n.numCols(), ERROR);
  }

}
