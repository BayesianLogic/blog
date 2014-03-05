package blog.distrib;

import java.util.List;

import blog.model.AbstractFunctionInterp;


public class LaserInterp extends AbstractFunctionInterp {

  public LaserInterp() {
    laserMaxRange = 10;
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
    return readingsForObstacle(
      laserX, laserY, laserTheta,
      laserAngles, laserMaxRange,
      obstacleX, obstacleY, obstacleR);
  }

  /**
   * Solve a quadratic equation.
   *
   * Returns two solutions in sorted order
   * Returns two nulls if there are no solutions.
   */
  public static Double[] solveQuadraticEquation(double a, double b, double c) {
    Double[] solutions = new Double[2];
    double delta = b * b - 4 * a * c;
    if (delta < 0) {
      solutions[0] = null;
      solutions[1] = null;
    } else {
      solutions[0] = (-b - Math.sqrt(delta)) / (2 * a);
      solutions[1] = (-b + Math.sqrt(delta)) / (2 * a);
    }
    return solutions;
  }

  /**
   * Return ground-truth laser readings given pose and obstacle.
   *
   * Laser sensor is at laserX, laserY, with orientation laserTheta.
   * Laser sensor shines a laser ray at each angle in laserAngles.
   * If the laser ray encounters no obstacle, its reading is laserMaxRange.
   *
   * Obstacle is a cylinder at obstacleX, obstacleY, of radius obstacleR.
   */
  public static double[] readingsForObstacle(
          double laserX, double laserY, double laserTheta,
          double[] laserAngles, double laserMaxRange,
          double obstacleX, double obstacleY, double obstacleR) {
    double[] readings = new double[laserAngles.length];
    for (int i = 0; i < laserAngles.length; i++) {
      final double angle = laserAngles[i];
      final double dx = laserX - obstacleX;
      final double dy = laserY - obstacleY;
      double a = 1;
      double b = ((2 * dx * Math.cos(laserTheta + angle)) +
                  (2 * dy * Math.sin(laserTheta + angle)));
      double c = dx * dx + dy * dy - obstacleR * obstacleR;
      Double[] solutions = solveQuadraticEquation(a, b, c);
      if (solutions[0] == null || solutions[1] < 0) {
        // Does not intersect ray.
        readings[i] = laserMaxRange;
      } else if (solutions[0] < 0) {
        readings[i] = solutions[1];
      } else {
        readings[i] = solutions[0];
      }
    }
    return readings;
  }

  private double laserMaxRange;
  private double[] laserAngles;
}
