package blog.common.numerical;

import blog.common.numerical.JamaMatrixLib;
import blog.common.numerical.MatrixLib;

/**
 * Creates MatrixLib objects.
 *
 * All code should use this class to create new MatrixLib objects, instead
 * of importing JamaMatrixLib directly. This allows us to easily change the
 * underlying implementation in the future.
 */
public class MatrixFactory {
  static public MatrixLib fromArray(double[][] array) {
    return new JamaMatrixLib(array);
  }

  static public MatrixLib eye(int size) {
    double[][] result = new double[size][size];
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        if (i == j) {
          result[i][j] = 1;
        } else {
          result[i][j] = 0;
        }
      }
    }
    return fromArray(result);
  }

  static public MatrixLib zeros(int rows, int cols) {
    double[][] result = new double[rows][cols];
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        result[i][j] = 0;
      }
    }
    return fromArray(result);
  }

  static public MatrixLib ones(int rows, int cols) {
    double[][] result = new double[rows][cols];
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        result[i][j] = 1;
      }
    }
    return fromArray(result);
  }

  static public MatrixLib vstack(MatrixLib a, MatrixLib b) {
    if (a.numCols() != b.numCols()) {
      throw new RuntimeException("matrices should have equal number of columns");
    }
    double[][] result = new double[a.numRows() + b.numRows()][a.numCols()];
    for (int i = 0; i < a.numRows(); i++) {
      for (int j = 0; j < a.numCols(); j++) {
        result[i][j] = a.elementAt(i, j);
      }
    }
    for (int i = 0; i < b.numRows(); i++) {
      for (int j = 0; j < b.numCols(); j++) {
        result[a.numRows() + i][j] = b.elementAt(i, j);
      }
    }
    return fromArray(result);
  }
}
