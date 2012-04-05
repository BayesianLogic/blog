package test;

import blog.common.Util;
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
		Matrix zero = new Matrix(5, 1);
		Matrix unit = Matrix.identity(5, 5);

		MultivarGaussian mvg = new MultivarGaussian(zero, unit);

		Matrix b = mvg.sampleVal();
		System.out.println(b.toString());
	}
}
