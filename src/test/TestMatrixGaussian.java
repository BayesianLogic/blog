package test;

import blog.common.Util;
import blog.common.numerical.JamaMatrixLib;
import blog.common.numerical.MatrixLib;
import blog.distrib.MatrixGaussian;
import Jama.Matrix;

/**
 * Print a value sampled from MatrixGaussian.
 */
public class TestMatrixGaussian {

    public static void main(String[] args) {
        Util.initRandom(true);

        double[][] locationTmp = new double[3][5];
        MatrixLib location = new JamaMatrixLib(locationTmp);

        double[][] uScaleTmp = new double[3][3];
        for (int i = 0; i < 3; i++) {
            uScaleTmp[i][i] = 1;
        }
        MatrixLib uScale = new JamaMatrixLib(uScaleTmp);

        double[][] vScaleTmp = new double[5][5];
        for (int i = 0; i < 5; i++) {
            vScaleTmp[i][i] = 1;
        }
        MatrixLib vScale = new JamaMatrixLib(vScaleTmp);

        MatrixGaussian dist = new MatrixGaussian(location, uScale, vScale);

        MatrixLib value = dist.sampleVal();
        System.out.println(value.toString());
    }
}
