package test;

import blog.common.Util;
import blog.common.numerical.JamaMatrixLib;
import blog.common.numerical.MatrixLib;
import blog.distrib.MultivarGaussian;
import Jama.Matrix;

/**
 * a small program to generate samples from Multivariate Gaussian Distribution.
 * 
 * @author leili
 * 
 */

public class TestMultivarGaussian {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Util.initRandom(true);
		
		double[][] zeroTmp = new double[5][1];
		MatrixLib zero = new JamaMatrixLib(zeroTmp);
		
		double[][] identityTmp = new double[5][5];
		for (int i = 0; i < 5; i++) {
			identityTmp[i][i] = 1;
		}
		MatrixLib unit = new JamaMatrixLib(identityTmp);

		MultivarGaussian mvg = new MultivarGaussian(zero, unit);

		MatrixLib b = mvg.sampleVal();
		System.out.println(b.toString());
	}
}
