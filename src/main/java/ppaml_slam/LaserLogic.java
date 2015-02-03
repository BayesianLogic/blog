package ppaml_slam;

import java.util.ArrayList;
import java.util.Random;

import blog.common.numerical.MatrixFactory;
import blog.common.numerical.MatrixLib;
import blog.distrib.IsotropicMultivarGaussian;

// NOTE: This file is directly ported from C / python, so the style not great.

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

    public String toString() {
      return "(" + x + ", " + y + ", " + r + ")";
    }
  };

  static public double defaultLaserMaxRange() {
    return 10.0;
  }

  static public double[] defaultLaserAngles() {
    double[] laserAngles = new double[361];
    for (int i = 0; i < 361; i++) {
      laserAngles[i] = (-90 + i * 0.5) * Math.PI / 180;
    }
    return laserAngles;
  }

  private static Double solveQuadraticEquation(double a, double b, double c) {
    double delta = b * b - 4 * a * c;
    if (delta < 0) {
      return null;
    } else {
      return (-b - Math.sqrt(delta)) / (2 * a);
    }
  }

  private static double square(double x) {
    return x * x;
  }

  private static double euclideanDist(double x, double y) {
    return Math.sqrt(square(x) + square(y));
  }

  // Return the index where to insert `key` into (sorted) `elems`.
  private static int bisect(double[] elems, double key) {
    int imin = 0;
    int imax = elems.length;
    while (imin < imax) {
      int imid = (imin + imax) / 2;
      assert (imid < imax);
      if (elems[imid] < key) {
        imin = imid + 1;
      } else {
        imax = imid;
      }
    }
    return imin;
  }

  // Return true iff the given ray hits the obstacle.
  private static boolean updateRay(double laser_x, double laser_y, double laser_theta, double obstacle_x,
      double obstacle_y, double obstacle_r, double laser_max_range, int i, double angle, double[] readings) {
    double a = 1.0;
    double b = (2.0 * (laser_x - obstacle_x) * Math.cos(laser_theta + angle) + 2.0 * (laser_y - obstacle_y)
        * Math.sin(laser_theta + angle));
    double c = (square(laser_x - obstacle_x) + square(laser_y - obstacle_y) - square(obstacle_r));
    Double x1 = solveQuadraticEquation(a, b, c);
    if (x1 == null) {
      return false;
    }
    assert (x1 >= 0 && x1 <= laser_max_range);
    if (x1 < readings[i]) {
      readings[i] = x1;
    }
    return true;
  }

  // Bring angle within [-PI, PI).
  // Currently works only for angles that are at most 2*PI off.
  private static double normalizeRadians(double theta) {
    if (theta < -Math.PI) {
      theta += 2 * Math.PI;
    } else if (theta > Math.PI) {
      theta -= 2 * Math.PI;
    }
    assert (theta >= -Math.PI && theta < Math.PI);
    return theta;
  }

  public static double[] readingsForObstacles(double laserX, double laserY, double laserTheta, double[] laserAngles,
      double laserMaxRange, ArrayList<Obstacle> obstacles) {
    assert (laserTheta >= -Math.PI);
    assert (laserTheta < Math.PI);

    double[] readings = new double[laserAngles.length];
    for (int a = 0; a < laserAngles.length; a++) {
      readings[a] = laserMaxRange;
    }

    for (Obstacle obst : obstacles) {
      double dist = euclideanDist(obst.x - laserX, obst.y - laserY);
      if (dist - obst.r <= 0) {
        continue; // obstacle overlaps with laser location
      }
      if (dist - obst.r >= laserMaxRange) {
        continue; // obstacle is too far
      }

      // Find a ray that hits the obstacle.
      double angleToObst = normalizeRadians(Math.atan2(obst.y - laserY, obst.x - laserX) - laserTheta);
      int index;
      if (angleToObst <= laserAngles[0]) {
        index = 0;
      } else if (angleToObst >= laserAngles[laserAngles.length - 1]) {
        index = laserAngles.length - 1;
      } else {
        index = bisect(laserAngles, angleToObst);
      }

      // Update rays to the left of that index.
      for (int i = index - 1; i >= 0; i--) {
        boolean hit = updateRay(laserX, laserY, laserTheta, obst.x, obst.y, obst.r, laserMaxRange, i, laserAngles[i],
            readings);
        if (!hit) {
          break;
        }
      }

      // Update rays to the right of that index.
      for (int i = index; i < laserAngles.length; i++) {
        boolean hit = updateRay(laserX, laserY, laserTheta, obst.x, obst.y, obst.r, laserMaxRange, i, laserAngles[i],
            readings);
        if (!hit) {
          break;
        }
      }
    }

    /*-
    System.out.println("laser_x = " + laserX);
    System.out.println("laser_y = " + laserY);
    System.out.println("laser_theta = " + laserTheta);
    System.out.print("java_readings = [");
    for (int i = 0; i < laserAngles.length; i++) {
      if (i > 0) {
        System.out.print(", ");
      }
      System.out.print(readings[i]);
    }
    System.out.println("]");
     */

    return readings;
  }

  public static ArrayList<Obstacle> extractObstacles(double laserX, double laserY, double laserTheta,
      double[] laser_angles, double laser_max_range, double[] obs_lasers) {
    // We assume the obstacle radius and noise params are known.
    final double known_radius = 0.37;
    final double true_laser_std = 0.1;
    final double scoringLaserCovScale = 2.0;
    final double min_improvement = 2.0;

    // Find segments where the laser readings are less than laser_max_range.
    int numSegments = 0;
    int segments[][] = new int[360][2];
    int last_start = -1;
    if (obs_lasers[0] < 0.9 * laser_max_range) {
      last_start = 0;
    }
    for (int i = 0; i < obs_lasers.length; i++) {
      double reading = obs_lasers[i];
      if (last_start != -1 && reading >= 0.9 * laser_max_range) {
        segments[numSegments][0] = last_start;
        segments[numSegments][1] = i - 1;
        numSegments++;
        last_start = -1;
      } else if (last_start == -1 && reading < 0.9 * laser_max_range) {
        last_start = i;
      }
    }
    if (last_start != -1) {
      segments[numSegments][0] = last_start;
      segments[numSegments][1] = obs_lasers.length - 1;
      numSegments++;
    }

    // Place an obstacle in the center of each of those segments.
    Random rng = new Random();
    Scorer scorer = new Scorer(laserX, laserY, laserTheta, laser_angles, laser_max_range, scoringLaserCovScale,
        obs_lasers);
    ArrayList<Obstacle> all_obstacles = new ArrayList<Obstacle>();
    double empty_score = scorer.calcScore(all_obstacles);
    for (int i = 0; i < numSegments; i++) {
      int start = segments[i][0];
      int stop = segments[i][1];
      int mid = (start + stop + 1) / 2;
      Obstacle bestObstacle = null;
      double best_improvement = Double.NEGATIVE_INFINITY;
      for (int trial = 0; trial < 10; trial++) {
        double noise = rng.nextGaussian() * true_laser_std;
        double distance = obs_lasers[mid] + known_radius + noise;
        Obstacle obstacle = new Obstacle();
        obstacle.x = laserX + distance * Math.cos(laserTheta + laser_angles[mid]);
        obstacle.y = laserY + distance * Math.sin(laserTheta + laser_angles[mid]);
        obstacle.r = known_radius;
        ArrayList<Obstacle> tmp = new ArrayList<Obstacle>();
        tmp.add(obstacle);
        double score = scorer.calcScore(tmp);
        double improvement = score - empty_score;
        if (improvement >= best_improvement) {
          bestObstacle = obstacle;
          best_improvement = improvement;
        }
      }
      if (best_improvement > min_improvement) {
        all_obstacles.add(bestObstacle);
      }
    }

    return all_obstacles;
  }

  public static class Scorer {
    private double laserX;
    private double laserY;
    private double laserTheta;
    private double[] laserAngles;
    private double laserMaxRange;
    private double[] obsLasers;
    private IsotropicMultivarGaussian gauss;

    public Scorer(double laserX, double laserY, double laserTheta, double[] laserAngles, double laserMaxRange,
        double scoringLaserCovScale, double[] obsLasers) {
      this.laserX = laserX;
      this.laserY = laserY;
      this.laserTheta = laserTheta;
      this.laserAngles = laserAngles;
      this.laserMaxRange = laserMaxRange;
      this.obsLasers = obsLasers;
      gauss = new IsotropicMultivarGaussian();
      MatrixLib mean = MatrixFactory.zeros(laserAngles.length, 1);
      gauss.setParams(mean, scoringLaserCovScale);
    }

    public double calcScore(ArrayList<Obstacle> obstacles) {
      double[] lasers = readingsForObstacles(laserX, laserY, laserTheta, laserAngles, laserMaxRange, obstacles);
      double[][] diff = new double[lasers.length][1];
      for (int i = 0; i < lasers.length; i++) {
        diff[i][0] = lasers[i] - obsLasers[i];
      }
      return gauss.getLogProb(MatrixFactory.fromArray(diff));
    }
  }
};
