package ppaml_car;

import java.util.ArrayList;


/**
 * Computes ground-truth laser readings given known pose and obstacles.
 *
 * This class is deliberately decoupled from BLOG, so that we can test it
 * independently. We plug it into BLOG using the LaserInterp class.
 */
public class LaserLogic {

  /**
   * Coordinates of a single obstacle.
   */
  public static class Obstacle {
    public double x;
    public double y;
    public double r;

    public Obstacle() {
      this(0, 0, 0);
    }

    public Obstacle(double x, double y, double r) {
      this.x = x;
      this.y = y;
      this.r = r;
    }
  };

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
      if (solutions[0] == null ||
              solutions[1] < 0 ||
              solutions[0] > laserMaxRange) {
        // Does not intersect ray, or intersects but too far.
        readings[i] = laserMaxRange;
      } else if (solutions[0] < 0) {
        readings[i] = solutions[1];
      } else {
        readings[i] = solutions[0];
      }
    }
    return readings;
  }

  public static double[] readingsForObstacles(
          double laserX, double laserY, double laserTheta,
          double[] laserAngles, double laserMaxRange,
          ArrayList<Obstacle> obstacles) {
    double[] readingsForAll = new double[laserAngles.length];
    for (int a = 0; a < laserAngles.length; a++) {
      readingsForAll[a] = laserMaxRange;
    }
    for (Obstacle obst : obstacles) {
      double[] readingsForOne = readingsForObstacle(
        laserX, laserY, laserTheta, laserAngles, laserMaxRange,
        obst.x, obst.y, obst.r);
      for (int a = 0; a < laserAngles.length; a++) {
        readingsForAll[a] = Math.min(readingsForAll[a], readingsForOne[a]);
      }
    }
    return readingsForAll;
  }
};
