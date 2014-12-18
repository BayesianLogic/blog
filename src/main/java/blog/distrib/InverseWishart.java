/**
 * 
 */
package blog.distrib;

import blog.common.numerical.MatrixFactory;
import blog.common.numerical.MatrixLib;

/**
 * Inverse Wishart distribution is the conjugate prior distribution for
 * Multivariate Gaussian distribution when the mean is fixed but the variance
 * is unknown. It has two parameters: A p by p square matrix illustrates the
 * scale and a integer mu > p - 1 illustrates the degrees of freedom.
 * 
 * @author Da Tang
 * @since July 14, 2014
 */
public class InverseWishart implements CondProbDistrib {

  /**
   * set parameters for Inverse_Wishart distribution
   * 
   * @param params
   *          an array of the form [MatrixLib, MatrixLib]
   *          <ul>
   *          <li>params[0]: <code>scale</code>, as a MatrixLib that serves as a
   *          scale matrix.</li>
   *          <li>params[1]: <code>freeDeg</code>, as a Integer that serves as a
   *          degree of freedom.</li>
   *          </ul>
   * 
   * @see blog.distrib.CondProbDistrib#setParams(java.util.List)
   */
  @Override
  public void setParams(Object[] params) {
    if (params.length != 2) {
      throw new IllegalArgumentException("expected two parameters");
    }
    setParams((MatrixLib) params[0], (Integer) params[1]);
  }

  /**
   * If the method scale parameter is not null and symmetric, then set the scale
   * parameter and the dimension parameter. If the degree parameter is not null
   * and positive, set the degree parameter. If the degree parameter is greater
   * than the dimension parameter - 1, initialize the constants.
   * 
   * @param scale
   *          parameter <code>scale</code>
   * @param freeDeg
   *          parameter <code>freeDeg</code>
   */
  public void setParams(MatrixLib scale, Integer freeDeg) {
    if (scale != null) {
      if (!(scale.numRows() > 0 && scale.isSymmetric())) {
        throw new IllegalArgumentException(
            "The scale given is not symmetric. It has dimensions "
                + scale.numRows() + " by " + scale.numCols() + ".");
      }
      this.scale = scale;
      this.hasScale = true;
    }
    if (freeDeg != null) {
      if (freeDeg <= 0) {
        throw new IllegalArgumentException(
            "The degrees of freedom given should be positive , but given "
                + freeDeg);
      }
      this.freeDeg = freeDeg;
      this.hasFreeDeg = true;
    }
    if (this.hasScale && this.hasFreeDeg) {
      if (freeDeg <= d - 1) {
        throw new IllegalArgumentException(
            "The freeDeg should be greater than the dimention of the scale matrix minus one, but given the freeDeg as "
                + this.freeDeg + " and the dimention as " + d + ".");
      }
      initializeConstants();
    }
  }

  /**
   * Initializes constants for the Inverse Wishart distribution.
   * Precondition: The current scale and degree parameters constitute a legal
   * assignment.
   */
  private void initializeConstants() {
    this.d = scale.numRows();
    this.dimFactor = 1 / (Math.pow(2, this.freeDeg * this.d * 0.5) * multivariategamma(
        this.d, this.freeDeg * 0.5));
    this.logDimFactor = -Math.log(2) * this.freeDeg * this.d * 0.5
        - lgmultivariategamma(this.d, this.freeDeg * 0.5);

    this.normConst = Math.pow(scale.det(), this.freeDeg * 0.5) * dimFactor;
    this.logNormConst = 0.5 * scale.logDet() * this.freeDeg + logDimFactor;
  }

  private static double multivariategamma(int p, double x) {
    return Math.exp(lgmultivariategamma(p, x));
  }

  private static double lgmultivariategamma(int p, double x) {
    double ret = Math.log(Math.PI) * p * (p - 1) * 0.25;
    for (int i = 1; i <= p; i++) {
      ret += Gamma.lgamma(x + (1 - i) * 0.5);
    }
    return ret;
  }

  private void checkHasParams() {
    if (!this.hasScale) {
      throw new IllegalArgumentException(
          "No scale provided for Inverse Wishart distribution.");
    }
    if (!this.hasFreeDeg) {
      throw new IllegalArgumentException(
          "No degrees of freedom provided for Inverse Wishart distribution.");
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

  /**
   * If <code>x</code> is a symmetric matrix of (d by d) and the scale and
   * freeDeg have been initialized correctly, return the density value.
   * 
   * @throws IllegalStateException
   *           if the matrix <code>x</code> is not a <code>d</code> by
   *           <code>d</code> symmetric matrix.
   * 
   * @param x
   *          symmetric matrix of dimension <code>d</code> by <code>d</code>
   */
  public double getProb(MatrixLib x) {
    checkHasParams();
    if (x.numRows() == d && x.isSymmetric()) {
      return Math.exp(-x.logDet() * (freeDeg + d + 1) * 0.5
          - scale.timesMat(x.inverse()).trace() * 0.5)
          * normConst;
    }
    throw new IllegalArgumentException(
        "The matrix given should be a symmetric one. But it isn't.");
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

  /**
   * Returns the natural log of the probability returned by getProb.
   * 
   * @param x
   *          row vector of dimension 1 by <code>d</code>
   */
  public double getLogProb(MatrixLib x) {
    checkHasParams();
    if (x.numRows() == d && x.isSymmetric()) {
      return -x.logDet() * (freeDeg + d + 1) * 0.5
          - scale.timesMat(x.inverse()).trace() * 0.5 + logNormConst;
    }
    throw new IllegalArgumentException(
        "The matrix given should be a symmetric one. But it isn't.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.distrib.CondProbDistrib#sampleVal()
   */
  public Object sampleVal() {
    return sample_value();
  }

  /**
   * Samples a value from this Inverse Wishart distribution by generating
   * <i>freeDeg</i> independent samples X_i's from Multivariate Gaussian with
   * zero means and scale's inverse as the covariance matrix. Then
   * \sum\limits_{i=1}^{freeDeg}X_i*X_i^T follows the Wishart distribution with
   * scale's inverse and freeDeg as the parameters. And its inverse matrix
   * follows our request (internal calls are ok if the private method
   * <code>initParams</code> is called first).
   */
  public MatrixLib sample_value() {
    checkHasParams();
    MatrixLib temp = MatrixFactory.zeros(d, d);
    MultivarGaussian tmp = new MultivarGaussian();
    tmp.setParams(MatrixFactory.zeros(d, 1), scale.inverse());
    for (int i = 0; i < freeDeg; i++) {
      MatrixLib tmpmat = tmp.sample_value();
      temp = temp.plus(tmpmat.timesMat(tmpmat.transpose()));
    }
    return temp.inverse();
  }

  @Override
  public Object[] getFiniteSupport() {
    return null;
  }

  private MatrixLib scale;
  private boolean hasScale;
  private int freeDeg;
  private boolean hasFreeDeg;

  private int d;
  private double dimFactor;
  private double logDimFactor;
  private double normConst;
  private double logNormConst;
}
