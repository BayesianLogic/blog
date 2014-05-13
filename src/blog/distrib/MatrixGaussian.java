package blog.distrib;

import java.util.List;

import blog.common.numerical.JamaMatrixLib;
import blog.common.numerical.MatrixLib;
import blog.model.MatrixSpec;
import blog.model.Type;

/**
 * Matrix Normal Distribution.
 *
 * Takes 3 parameters, which are fixed at construction time:
 * - location (real n x p matrix)
 * - uScale (positive-definite real n x n matrix)
 * - vScale (positive-definite real p x p matrix)
 */
public class MatrixGaussian extends AbstractCondProbDistrib {

  public MatrixGaussian(
        MatrixLib location, MatrixLib uScale, MatrixLib vScale) {
    initParams(location, uScale, vScale);
  }

  public MatrixGaussian(List params) {
    if (params.size() != 3) {
      throw new IllegalArgumentException(
        "MatrixGaussian expects 3 parameters.");
    }
    initParams(
      (MatrixLib)params.get(0),
      (MatrixLib)params.get(1),
      (MatrixLib)params.get(2));
  }

  private void initParams(
        MatrixLib location, MatrixLib uScale, MatrixLib vScale) {
    if (location.numRows() != uScale.numRows() ||
        location.numRows() != uScale.numCols()) {
      throw new IllegalArgumentException(
        "uScale does not match dimension of location");
    }
    if (location.numCols() != vScale.numRows() ||
        location.numCols() != vScale.numCols()) {
      throw new IllegalArgumentException(
        "vScale does not match dimension of location");
    }

    this.location = location;
    this.uScale = uScale;
    this.vScale = vScale;

    uScaleInv = uScale.inverse();
    vScaleInv = vScale.inverse();
    uScaleSqrt = uScale.choleskyFactor();
    vScaleSqrt = vScale.choleskyFactor().transpose();
    logDenom = (
      location.numRows() * location.numCols() / 2 * Math.log(2 * Math.PI) +
      location.numRows() / 2 * Math.log(vScale.det()) +
      location.numCols() / 2 * Math.log(uScale.det()));
  }

  public double getProb(List args, Object value) {
    return getProb((MatrixLib)value);
  }

  public double getProb(MatrixLib value) {
    return Math.exp(getLogProb(value));
  }

  public double getLogProb(List args, Object value) {
    return getLogProb((MatrixLib)value);
  }

  public double getLogProb(MatrixLib value) {
    if (value.numRows() != location.numRows() ||
        value.numCols() != location.numCols()) {
      throw new IllegalArgumentException(
        "value does not match dimensions of distribution");
    }
    // See wikipedia article (or Arnold book mentioned below) for the PDF.
    MatrixLib xMinusM = value.minus(location);
    double bigTrace = (vScaleInv.timesMat(xMinusM.transpose())
      .timesMat(uScaleInv).timesMat(xMinusM).trace());
    return -0.5 * bigTrace - logDenom;
  }

  public Object sampleVal(List args, Type childType) {
    return sampleVal();
  }

  /**
   * Sample a value from this distribution.
   *
   * See Arnold, S.F. (1981), "The theory of linear models and multivariate
   * analysis", section 17.2. Basically, we generate a n x p matrix where the
   * entries are iid standard normal. Then we transform this matrix to have
   * the desired parameters of the matrix normal distribution.
   */
  public MatrixLib sampleVal() {
    double[][] mat = new double[location.numRows()][location.numCols()];
    for (int r = 0; r < location.numRows(); r++) {
      for (int c = 0; c < location.numCols(); c++) {
        mat[r][c] = UnivarGaussian.STANDARD.sampleVal();
      }
    }
    MatrixLib temp = new JamaMatrixLib(mat);
    return location.plus(uScaleSqrt.timesMat(temp.timesMat(vScaleSqrt)));
  }

  public MatrixLib getLocation() {
    return location;
  }

  public MatrixLib getUScale() {
    return uScale;
  }

  public MatrixLib getVScale() {
    return vScale;
  }

  // Parameters:
  private MatrixLib location;
  private MatrixLib uScale;
  private MatrixLib vScale;

  // Handy values precomputed on initialization:
  private MatrixLib uScaleInv;
  private MatrixLib vScaleInv;
  private MatrixLib uScaleSqrt;
  private MatrixLib vScaleSqrt;
  private double logDenom;
}

