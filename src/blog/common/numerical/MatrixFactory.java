package blog.common.numerical;

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

  static public MatrixLib createVector(double... args) {
    double[][] ary = new double[1][args.length];
    for (int i = 0; i < args.length; i++) {
      ary[0][i] = args[i];
    }
    return MatrixFactory.fromArray(ary);
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

  /**
   * Returns a MatrixLib row vector of dimension 1 by args.length. args is
   * interpreted as an array of doubles that comprise the first and only row for
   * the returned MatrixLib instance.
   */
  static public MatrixLib createRowVector(double... args) {
    double[][] ary = new double[1][args.length];
    System.arraycopy(args, 0, ary[0], 0, args.length);
    return MatrixFactory.fromArray(ary);
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

  static public MatrixLib hstack(MatrixLib a, MatrixLib b) {
    if (a.numRows() != b.numRows()) {
      throw new RuntimeException("matrices should have equal number of rows");
    }
    double[][] result = new double[a.numRows()][a.numCols() + b.numCols()];
    for (int i = 0; i < a.numRows(); i++) {
      for (int j = 0; j < a.numCols(); j++) {
        result[i][j] = a.elementAt(i, j);
      }
    }
    for (int i = 0; i < b.numRows(); i++) {
      for (int j = 0; j < b.numCols(); j++) {
        result[i][a.numCols() + j] = b.elementAt(i, j);
      }
    }
    return fromArray(result);
  }
}
