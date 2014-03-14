package ppaml_quadcopter;

import java.util.List;

import blog.common.numerical.MatrixFactory;
import blog.model.AbstractFunctionInterp;


public class LaserInterp extends AbstractFunctionInterp {

  public LaserInterp(List objs) {
    laserMaxRange = 10;
    laserAngles = new double[361];
    for (int i = 0; i < 361; i++) {
      laserAngles[i] = (-90 + i * 0.5) * Math.PI / 180;
    }
  }

  /**
   * Compute ground-truth laser readings.
   *
   * Input: laserX, laserY, laserTheta, obstacleX, obstacleY, obstacleR.
   * Output: double[361] laser readings.
   */
  public Object getValue(List args) {
    if (args.size() != 6) {
      throw new IllegalArgumentException("LaserInterp expected 6 arguments");
    }
    double laserX = (double) args.get(0);
    double laserY = (double) args.get(1);
    double laserTheta = (double) args.get(2);
    double obstacleX = (double) args.get(3);
    double obstacleY = (double) args.get(4);
    double obstacleR = (double) args.get(5);
    double[] readings = LaserLogic.readingsForObstacle(
      laserX, laserY, laserTheta,
      laserAngles, laserMaxRange,
      obstacleX, obstacleY, obstacleR);

    // Convert to MatrixLib.
    // Additional step required because MatrixLib only takes a double[][].
    double[][] tmp = new double[readings.length][1];
    for (int i = 0; i < readings.length; i++) {
      tmp[i][0] = readings[i];
    }
    return MatrixFactory.fromArray(tmp);
  }

  private double laserMaxRange;
  private double[] laserAngles;
}
