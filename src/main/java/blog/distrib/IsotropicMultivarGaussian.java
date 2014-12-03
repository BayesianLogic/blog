package blog.distrib;

import blog.common.numerical.MatrixFactory;
import blog.common.numerical.MatrixLib;

/**
 * Isotropic Multivariate Gaussian (normal) distribution with parameters
 * <i>mean</i> and <i>covarianceScale</i>. The covariance matrix is identity
 * times <i>covarianceScale</i>.
 * 
 * @author cberzan
 * @since December 2, 2014
 */
public class IsotropicMultivarGaussian implements CondProbDistrib {

  /**
   * set parameters for Isotropic Multivariate Gaussian distribution
   * 
   * @param params
   *          an array of the form [MatrixLib, Real]
   *          <ul>
   *          <li>params[0]: <code>mean</code>, a column vector MatrixLib</li>
   *          <li>params[1]: <code>covarianceScale</code>, a Real</li>
   *          </ul>
   * 
   * @see blog.distrib.CondProbDistrib#setParams(java.util.List)
   */
  @Override
  public void setParams(Object[] params) {
    if (params.length != 2) {
      throw new IllegalArgumentException("expected two parameters");
    }
    setParams((MatrixLib) params[0], (Number) params[1]);
  }

  public void setParams(MatrixLib mean, Number covarianceScale) {
    if (mean != null) {
      if (!(mean.numRows() > 0 && mean.numCols() == 1)) {
        throw new IllegalArgumentException(
            "The mean given is not a valid column vector. It has dimensions "
                + mean.numRows() + " by " + mean.numCols() + ".");
      }
      this.mean = mean;
      this.hasMean = true;
    }
    if (covarianceScale != null) {
      if (covarianceScale.doubleValue() <= 0.0) {
        throw new IllegalArgumentException(
            "The covarianceScale parameter must be positive");
      }
      this.covarianceScale = covarianceScale.doubleValue();
      this.hasCovarianceScale = true;
    }
    if (this.hasMean && this.hasCovarianceScale) {
      initializeConstants();
    }
  }

  /**
   * Initializes constants for the Multivariate Gaussian.
   * Precondition: The current covariance and mean parameters constitute a legal
   * assignment.
   */
  private void initializeConstants() {
    d = mean.numRows();
    sqrtCovarianceScale = Math.sqrt(covarianceScale);
    double logdet = Math.log(covarianceScale) * d;
    logNormConst = -0.5 * (d * Math.log(2 * Math.PI) + logdet);
  }

  private void checkHasParams() {
    if (!this.hasMean) {
      throw new IllegalArgumentException("No mean provided");
    }
    if (!this.hasCovarianceScale) {
      throw new IllegalArgumentException("No covarianceScale provided");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#getProb(java.lang.Object)
   */
  @Override
  public double getProb(Object value) {
    return getProb((MatrixLib) value);
  }

  public double getProb(MatrixLib x) {
    return Math.exp(getLogProb(x));
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#getLogProb(java.lang.Object)
   */
  @Override
  public double getLogProb(Object value) {
    return getLogProb((MatrixLib) value);
  }

  public double getLogProb(MatrixLib x) {
    checkHasParams();
    if (x.numRows() == d && x.numCols() == 1) {
      return logNormConst - 0.5
          * x.minus(mean).transpose().timesMat(x.minus(mean)).elementAt(0, 0)
          / covarianceScale;
    }
    throw new IllegalArgumentException("The matrix given is " + x.numRows()
        + " by " + x.numCols() + " but should be a " + d + " by 1 vector.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#sampleVal()
   */
  public Object sampleVal() {
    return sample_value();
  }

  public MatrixLib sample_value() {
    checkHasParams();
    double[][] mat = new double[d][1];
    for (int i = 0; i < d; i++) {
      double z = UnivarGaussian.STANDARD.sample_value();
      mat[i][0] = mean.elementAt(i, 0) + z * sqrtCovarianceScale;
    }
    return MatrixFactory.fromArray(mat);
  }

  @Override
  public Object[] getFiniteSupport() {
    return null;
  }

  private MatrixLib mean;
  private boolean hasMean;
  private double covarianceScale;
  private boolean hasCovarianceScale;

  private int d;
  private double sqrtCovarianceScale;
  private double logNormConst;
}
