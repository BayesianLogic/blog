package test;

import Jama.Matrix;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import blog.common.Util;
import blog.common.numerical.JamaMatrixLib;
import blog.common.numerical.MatrixLib;
import blog.distrib.MultivarGaussian;


/**
 * Unit tests for MultivarGaussian.
 */
@RunWith(JUnit4.class)
public class TestMultivarGaussian {

  @Test
  public void testSample() {
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
    // System.out.println(b.toString());
  }
}
