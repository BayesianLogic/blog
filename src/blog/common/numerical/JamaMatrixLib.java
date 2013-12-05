package blog.common.numerical;

import Jama.Matrix;

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
  public int rowLen() {
    return values.getRowDimension();
  }

  @Override
  public int colLen() {
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
  public boolean equals(Object obj) {
    // in general you need to check the dimensionality of the matrix as well.
    if (obj instanceof JamaMatrixLib) {
      return values.minus(((JamaMatrixLib) obj).values).normInf() < ZERO_THRESHOLD;
    }
    return false;
  }
}
