package blog.common.numerical;

import java.util.Arrays;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

/**
 * Matrix and linear algebra operations
 * 
 * @author awong
 * @author leili
 * @date Feb 11, 2014
 */
public class JamaMatrixLib implements MatrixLib {

  private Matrix values;
  private static double ZERO_THRESHOLD = 1e-20;

  public JamaMatrixLib(double[][] contents) {
    values = new Matrix(contents);
  }

  public JamaMatrixLib(Matrix contents) {
    values = contents;
  }

  @Override
  public double elementAt(int i, int j) {
    return values.get(i, j);
  }

  @Override
  public void setElement(int x, int y, double val) {
    values.set(x, y, val);
  }

  @Override
  public String toString() {
    return values.toString();
  }

  @Override
  public int numRows() {
    return values.getRowDimension();
  }

  @Override
  public int numCols() {
    return values.getColumnDimension();
  }

  @Override
  public MatrixLib sliceRow(int i) {
    return new JamaMatrixLib(values.getMatrix(i, i, 0,
        values.getColumnDimension() - 1));
  }

  @Override
  public MatrixLib plus(MatrixLib otherMat) {
    if (otherMat instanceof JamaMatrixLib) {
      JamaMatrixLib newMat = (JamaMatrixLib) otherMat;
      return new JamaMatrixLib(values.plus(newMat.values));
    }
    throw new ClassCastException(
        "Only one matrix library should be in use at a time!");
  }

  @Override
  public MatrixLib minus(MatrixLib otherMat) {
    if (otherMat instanceof JamaMatrixLib) {
      JamaMatrixLib newMat = (JamaMatrixLib) otherMat;
      return new JamaMatrixLib(values.minus(newMat.values));
    }
    throw new ClassCastException(
        "Only one matrix library should be in use at a time!");
  }

  @Override
  public MatrixLib timesScale(double scale) {
    return new JamaMatrixLib(values.times(scale));
  }

  @Override
  public MatrixLib timesMat(MatrixLib otherMat) {
    if (otherMat instanceof JamaMatrixLib) {
      JamaMatrixLib newMat = (JamaMatrixLib) otherMat;
      return new JamaMatrixLib(values.times(newMat.values));
    }
    throw new ClassCastException(
        "Only one matrix library should be in use at a time!");
  }

  @Override
  public double det() {
    return values.det();
  }

  @Override
  public double logDet() {
    double logDet = 0.0;
    for (double val : eigenvals()) {
      logDet += Math.log(val);
    }
    return logDet;
  }

  @Override
  public MatrixLib transpose() {
    return new JamaMatrixLib(values.transpose());
  }

  @Override
  public MatrixLib inverse() {
    return new JamaMatrixLib(values.inverse());
  }

  @Override
  public MatrixLib choleskyFactor() {
    return new JamaMatrixLib(values.chol().getL());
  }

  @Override
  public MatrixLib columnSum() {
    double[][] result = new double[1][numCols()];
    for (int i = 0; i < numCols(); i++) {
      result[0][i] = 0;
      for (int j = 0; j < numRows(); j++) {
        result[0][i] += elementAt(j, i);
      }
    }
    return new JamaMatrixLib(result);
  }

  @Override
  public double[] eigenvals() {
    EigenvalueDecomposition decomp = new EigenvalueDecomposition(values);
    return decomp.getRealEigenvalues();
  }

  @Override
  public boolean equals(Object obj) {
    // in general you need to check the dimensionality of the matrix as well.
    if (obj instanceof JamaMatrixLib) {
      return Arrays.deepEquals(values.getArray(),
          ((JamaMatrixLib) obj).values.getArray());
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return Arrays.deepHashCode(values.getArray());
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.common.numerical.MatrixLib#exp()
   */
  @Override
  public MatrixLib exp() {
    double[][] v = values.getArrayCopy();
    for (int i = 0; i < numRows(); i++) {
      for (int j = 0; j < numCols(); j++)
        v[i][j] = Math.exp(v[i][j]);
    }
    return new JamaMatrixLib(v);
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.common.numerical.MatrixLib#abs()
   */
  @Override
  public MatrixLib abs() {
    double[][] v = values.getArrayCopy();
    for (int i = 0; i < numRows(); i++) {
      for (int j = 0; j < numCols(); j++)
        v[i][j] = Math.abs(v[i][j]);
    }
    return new JamaMatrixLib(v);
  }
}
