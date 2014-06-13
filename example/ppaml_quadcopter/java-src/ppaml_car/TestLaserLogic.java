package ppaml_car;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public class TestLaserLogic {

  @Test
  public void testSolveQuadraticEquation() {
    Double[] solutions = LaserLogic.solveQuadraticEquation(123, 45, 67);
    assertEquals(solutions[0], null);
    assertEquals(solutions[1], null);

    solutions = LaserLogic.solveQuadraticEquation(123, 45, -67);
    assertEquals(solutions[0], -0.94330678167407689, 1e-10);
    assertEquals(solutions[1], 0.57745312313749153, 1e-10);
  }

  @Test
  public void testReadingsForObstacle() {
    double laserX = 2;
    double laserY = 3;
    double laserTheta = 0.3;
    double[] laserAngles = new double[]{
      0.1, 0.3, 0.5, 0.7, 0.9, 1.1, 1.3, 1.5, 1.7};
    double laserMaxRange = 10;
    double obstacleX = 7;
    double obstacleY = 9;
    double obstacleR = 2;
    double[] referenceReadings = new double[]{
      10, 10, 5.8777472, 5.99884367, 10, 10, 10, 10, 10};
    double readings[] = LaserLogic.readingsForObstacle(
      laserX, laserY, laserTheta,
      laserAngles, laserMaxRange,
      obstacleX, obstacleY, obstacleR);
    assertEquals(laserAngles.length, referenceReadings.length);
    assertEquals(readings.length, referenceReadings.length);
    for (int i = 0; i < laserAngles.length; i++) {
      assertEquals(readings[i], referenceReadings[i], 1e-7);
    }
  }

  @Test
  public void testReadingsForObstacles() {
    double laserX = 2;
    double laserY = 3;
    double laserTheta = 0.3;
    double[] laserAngles = new double[]{
      0.1, 0.3, 0.5, 0.7, 0.9, 1.1, 1.3, 1.5, 1.7};
    double laserMaxRange = 10;
    ArrayList<LaserLogic.Obstacle> obstacles = new ArrayList<LaserLogic.Obstacle>();
    obstacles.add(new LaserLogic.Obstacle(7, 9, 2));
    obstacles.add(new LaserLogic.Obstacle(8, 5, 1));
    double[] referenceReadings = new double[]{
      5.43596135, 10, 5.8777472, 5.99884367, 10, 10, 10, 10, 10};
    double readings[] = LaserLogic.readingsForObstacles(
      laserX, laserY, laserTheta,
      laserAngles, laserMaxRange, obstacles);
    assertEquals(laserAngles.length, referenceReadings.length);
    assertEquals(readings.length, referenceReadings.length);
    for (int i = 0; i < laserAngles.length; i++) {
      assertEquals(readings[i], referenceReadings[i], 1e-7);
    }
  }
}
