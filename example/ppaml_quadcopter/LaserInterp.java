package ppaml_quadcopter;

import java.util.ArrayList;
import java.util.List;

import blog.common.numerical.MatrixFactory;
import blog.common.numerical.MatrixLib;
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
   * Input: laserX, laserY, laserTheta, obstacleXs, obstacleYs, obstacleRs.
   * Output: double[361] laser readings.
   */
  public Object getValue(List args) {
    if (args.size() != 6) {
      throw new IllegalArgumentException("LaserInterp expected 6 arguments");
    }
    double laserX = (double) args.get(0);
    double laserY = (double) args.get(1);
    double laserTheta = (double) args.get(2);
    double[] obstacleXs = squeezeDoublesFromObject(args.get(3));
    double[] obstacleYs = squeezeDoublesFromObject(args.get(4));
    double[] obstacleRs = squeezeDoublesFromObject(args.get(5));
    double[] readings = LaserLogic.readingsForObstacles(
      laserX, laserY, laserTheta,
      laserAngles, laserMaxRange,
      obstacleXs, obstacleYs, obstacleRs);

    // Convert to MatrixLib.
    // Additional step required because MatrixLib only takes a double[][].
    double[][] tmp = new double[readings.length][1];
    for (int i = 0; i < readings.length; i++) {
      tmp[i][0] = readings[i];
    }
    return MatrixFactory.fromArray(tmp);
  }

  /**
   * Convert an Object which is really an ArrayList<Double> to double[].
   */
  private static double[] squeezeDoublesFromObject(Object obj) {
    // Note: If I cast it to ArrayList<Double> directly, I get a stupid
    // "unchecked cast" warning, because of "type erasure". So I have to
    // cast it to a generic ArrayList here, and then cast each element.
    ArrayList list = (ArrayList) obj;
    double[] result = new double[list.size()];
    for (int i = 0; i < list.size(); i++) {
      result[i] = (double) list.get(i);
    }
    return result;
  }

  private double laserMaxRange;
  private double[] laserAngles;
}
