package ppaml_slam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import blog.common.numerical.MatrixFactory;
import blog.common.numerical.MatrixLib;
import blog.model.AbstractFunctionInterp;

public class LaserInterp extends AbstractFunctionInterp {

  public LaserInterp(List objs) {
    laserMaxRange = LaserLogic.defaultLaserMaxRange();
    laserAngles = LaserLogic.defaultLaserAngles();
  }

  /**
   * Compute ground-truth laser readings.
   * 
   * Input: laserX, laserY, laserTheta, obstacles, where obstacles is either a
   * set of (x, y, r) lists, or a matrix with (x, y, r) rows.
   * 
   * Output: double[361] laser readings.
   */
  public Object getValue(List args) {
    // Parse input args.
    if (args.size() != 4) {
      throw new IllegalArgumentException("LaserInterp expected 6 arguments");
    }
    double laserX = (Double) args.get(0);
    double laserY = (Double) args.get(1);
    double laserTheta = (Double) args.get(2);
    Object rawObstacles = args.get(3);
    ArrayList<LaserLogic.Obstacle> obstacles;
    if (rawObstacles instanceof Collection) {
      obstacles = parseObstaclesFromSet((Collection) rawObstacles);
    } else if (rawObstacles instanceof MatrixLib) {
      obstacles = parseObstaclesFromMatrix((MatrixLib) rawObstacles);
    } else {
      throw new IllegalArgumentException("obstacles must be set or matrix");
    }

    // Compute laser readings.
    double[] readings = LaserLogic.readingsForObstacles(laserX, laserY,
        laserTheta, laserAngles, laserMaxRange, obstacles);

    // Convert result to MatrixLib.
    // Additional step required because MatrixLib only takes a double[][].
    double[][] tmp = new double[readings.length][1];
    for (int i = 0; i < readings.length; i++) {
      tmp[i][0] = readings[i];
    }
    return MatrixFactory.fromArray(tmp);
  }

  private static ArrayList<LaserLogic.Obstacle> parseObstaclesFromSet(
      Collection rawObstacles) {
    // rawObstacles is a blog.common.HashMultiset of ArrayLists.
    ArrayList<LaserLogic.Obstacle> obstacles = new ArrayList<LaserLogic.Obstacle>();
    for (Object obj : rawObstacles) {
      ArrayList coords = (ArrayList) obj;
      obstacles.add(new LaserLogic.Obstacle((Double) coords.get(0),
          (Double) coords.get(1), (Double) coords.get(2)));
    }
    return obstacles;
  }

  private static ArrayList<LaserLogic.Obstacle> parseObstaclesFromMatrix(
      MatrixLib rawObstacles) {
    // rawObstacles is a blog.common.numerical.MatrixLib with (x, y, r) rows.
    ArrayList<LaserLogic.Obstacle> obstacles = new ArrayList<LaserLogic.Obstacle>();
    for (int r = 0; r < rawObstacles.numRows(); r++) {
      obstacles.add(new LaserLogic.Obstacle(rawObstacles.elementAt(r, 0),
          rawObstacles.elementAt(r, 1), rawObstacles.elementAt(r, 2)));
    }
    return obstacles;
  }

  private double laserMaxRange;
  private double[] laserAngles;
}
