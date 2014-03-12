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
}
