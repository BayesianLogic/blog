package test.blog;

import blog.common.numerical.MatrixFactory;
import blog.common.numerical.MatrixLib;


/**
 * Compare performance for two ways to compute the log determinant.
 *
 * This is a standalone program.
 */
public class BenchmarkLogDet {
  public static void main(String[] args) {
    MatrixLib matrix = MatrixFactory.eye(400).timesScale(0.001);
    final int trials = 300;
    double result = 0;

    // Using eigenvalue decomposition:
    long nanoStart = System.nanoTime();
    for (int t = 0; t < trials; t++) {
      result = computeLogDetUsingEigenDecomp(matrix);
    }
    double secPerTrialEigenDecomp = (System.nanoTime() - nanoStart) / (trials * 1e9);
    System.out.println(result);
    System.out.println("eigen decomp: " + secPerTrialEigenDecomp + " sec / trial");

    // Using Cholesky decomposition:
    nanoStart = System.nanoTime();
    for (int t = 0; t < trials; t++) {
      result = computeLogDetUsingCholeskyDecomp(matrix);
    }
    double secPerTrialCholeskyDecomp = (System.nanoTime() - nanoStart) / (trials * 1e9);
    System.out.println(result);
    System.out.println("chol  decomp: " + secPerTrialCholeskyDecomp + " sec / trial");
  }

  public static double computeLogDetUsingEigenDecomp(MatrixLib matrix) {
    double logDet = 0.0;
    for (double val : matrix.eigenvals()) {
      logDet += Math.log(val);
    }
    return logDet;
  }

  public static double computeLogDetUsingCholeskyDecomp(MatrixLib matrix) {
    MatrixLib chol = matrix.choleskyFactor();
    double logDet = 0.0;
    for (int i = 0; i < chol.colLen(); i++) {
      logDet += Math.log(chol.elementAt(i, i));
    }
    logDet *= 2;
    return logDet;
  }
}
