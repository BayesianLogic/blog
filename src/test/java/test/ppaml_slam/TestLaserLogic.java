package test.ppaml_slam;

import ppaml_slam.LaserLogic;
import ppaml_slam.LaserLogic.Obstacle;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestLaserLogic {

  @Test
  public void testReadingsForObstacles() {
    double laserX = 2;
    double laserY = 3;
    double laserTheta = 0.3;
    double[] laserAngles = new double[] { 0.1, 0.3, 0.5, 0.7, 0.9, 1.1, 1.3, 1.5, 1.7 };
    double laserMaxRange = 10;
    ArrayList<LaserLogic.Obstacle> obstacles = new ArrayList<LaserLogic.Obstacle>();
    obstacles.add(new LaserLogic.Obstacle(7, 9, 2));
    obstacles.add(new LaserLogic.Obstacle(8, 5, 1));
    double[] referenceReadings = new double[] { 5.43596135, 10, 5.8777472, 5.99884367, 10, 10, 10, 10, 10 };
    double readings[] = LaserLogic.readingsForObstacles(laserX, laserY, laserTheta, laserAngles, laserMaxRange,
        obstacles);
    assertEquals(laserAngles.length, referenceReadings.length);
    assertEquals(readings.length, referenceReadings.length);
    for (int i = 0; i < laserAngles.length; i++) {
      assertEquals(readings[i], referenceReadings[i], 1e-7);
    }
  }

  @Test
  public void testScorer() {
    // Test against Python implementation.
    double lx = -5.8043533665402158;
    double ly = 2.4373463007300311e-05;
    double lt = -0.000112593;
    double[] laserAngles = LaserLogic.defaultLaserAngles();
    double laserMaxRange = LaserLogic.defaultLaserMaxRange();
    double scoringLaserCovScale = 2.0;
    double[] obsLasers = { 9.99112, 10.1518, 9.96472, 10.0942, 10.0306, 10.0467, 10.0576, 10.0147, 10.0248, 10.1041,
        9.92387, 9.9082, 9.89024, 10.0892, 9.96818, 10.0904, 10.0177, 9.9872, 10.0542, 10.0842, 10.0892, 10.2483,
        10.0916, 10.12, 10.0609, 10.0439, 10.1086, 9.96434, 10.0278, 10.0384, 10.2351, 9.9389, 10.0557, 9.8775,
        9.87645, 10.089, 9.91507, 10.0773, 9.92106, 10.1403, 9.76055, 10.0453, 10.0433, 10.0836, 10.1515, 10.0739,
        10.0093, 10.0195, 10.0079, 10.0704, 10.0284, 9.99141, 10.0342, 10.0513, 10.0596, 9.99998, 9.97684, 10.0724,
        10.1458, 10.1059, 9.93182, 9.95667, 9.91674, 9.87357, 10.0217, 10.0352, 9.9022, 9.8477, 9.79345, 9.86417,
        10.115, 10.0853, 10.1079, 9.91121, 9.9993, 10.0671, 10.067, 10.0597, 10.0264, 9.98759, 10.0048, 10.0515,
        5.22403, 5.24101, 5.12683, 5.08631, 5.15997, 5.0158, 5.12577, 5.07404, 5.06636, 5.01376, 5.02826, 5.18655,
        4.93637, 5.10793, 5.25499, 5.37785, 9.90489, 9.8883, 10.0262, 9.99327, 9.9627, 10.0871, 10.0318, 10.0479,
        10.0319, 9.99507, 3.16966, 3.37584, 2.9762, 3.00049, 3.03738, 3.091, 2.76963, 2.92976, 2.79277, 2.7931,
        2.69354, 3.09668, 2.86839, 2.78102, 2.66023, 2.66168, 2.85464, 2.85977, 3.03943, 2.86236, 2.85527, 2.79503,
        3.14345, 2.99083, 2.92165, 3.08306, 3.22666, 9.87075, 10.0483, 10.0792, 10.0711, 9.89687, 6.59919, 6.58177,
        6.53691, 6.5357, 6.56482, 6.57846, 6.34427, 6.18941, 6.56314, 6.26824, 6.45965, 6.57841, 6.98979, 10.0567,
        9.8893, 9.9502, 9.74533, 10.1335, 10.1655, 9.98709, 9.94385, 10.0421, 10.0377, 10.0875, 9.9222, 9.89828,
        10.0212, 9.87701, 9.8076, 10.1046, 10.0156, 10.0103, 9.97956, 10.0663, 10.0862, 9.81722, 10.1458, 10.0705,
        9.9629, 9.93532, 9.94897, 9.91343, 10.1665, 9.92292, 10.1453, 10.0974, 9.90657, 9.91919, 10.1305, 9.85292,
        9.8227, 10.004, 10.0079, 10.0492, 10.0654, 9.94849, 10.0473, 4.40004, 4.53018, 4.25113, 4.28116, 4.37335,
        4.22752, 4.20528, 4.32295, 4.41131, 4.33851, 4.31093, 4.26113, 4.36962, 4.10706, 4.119, 4.24227, 4.29144,
        4.42043, 4.58056, 10.1121, 10.126, 10.115, 10.1073, 10.0027, 10.0266, 10.0124, 10.063, 9.98801, 10.0029,
        9.97396, 10.1809, 9.95998, 9.97003, 9.93945, 9.92735, 10.058, 9.93308, 10.0525, 10.0501, 9.98912, 10.0982,
        10.0766, 9.98516, 9.98273, 10.0959, 10.0152, 9.96086, 10.0043, 10.0369, 9.98806, 10.0551, 10.1127, 9.99077,
        9.99855, 10.1169, 10.0209, 1.8489, 1.77903, 1.85174, 1.57384, 1.88631, 1.79471, 1.52312, 1.66396, 1.61406,
        1.54994, 1.74775, 1.64259, 1.64876, 1.59086, 1.50159, 1.82249, 1.6199, 1.66409, 1.52228, 1.74092, 1.79667,
        1.53008, 1.50714, 1.52771, 1.64773, 1.623, 1.63559, 1.51077, 1.6481, 1.7745, 1.71953, 1.40376, 1.64063,
        1.49152, 1.62863, 1.75321, 1.75372, 1.61545, 1.69677, 1.60412, 1.76384, 1.71561, 1.98379, 1.80654, 1.98831,
        9.98531, 10.2139, 9.97562, 9.83308, 9.88611, 9.95925, 9.86667, 10.1035, 9.96003, 10.112, 10.1505, 10.051,
        10.1987, 9.85437, 10.0424, 9.9402, 9.95207, 10.0513, 9.97947, 9.82434, 10.0792, 9.92583, 9.68906, 9.92674,
        10.1022, 9.81909, 9.9495, 10.0785, 10.0497, 10.0447, 10.0375, 10.1026, 9.96022, 9.89837, 9.92919, 10.0884,
        10.0211, 9.96716, 9.95919, 9.9447, 10.0652, 9.89257, 10.0347, 10.0075, 10.2174, 10.1708, 9.97744, 9.9607,
        10.016, 10.1277, 9.96755, 9.95952, 9.94743, 9.94982, 10.0254, 9.98185, 10.0474, 9.95918, 9.76251, 9.93913,
        10.0674, 9.95075, 10.2174 };
    LaserLogic.Scorer scorer = new LaserLogic.Scorer(lx, ly, lt, laserAngles, laserMaxRange, scoringLaserCovScale,
        obsLasers);
    ArrayList<LaserLogic.Obstacle> obstacles = new ArrayList<LaserLogic.Obstacle>();
    double emptyScore = scorer.calcScore(obstacles);
    double refEmptyScore = -1862.059445078608;
    assertEquals(refEmptyScore, emptyScore, 1e-7);
    obstacles.add(new LaserLogic.Obstacle(-1.96662485, -3.83856845, 0.37));
    obstacles.add(new LaserLogic.Obstacle(-2.99756189, -1.58839905, 0.37));
    double twoObstScore = scorer.calcScore(obstacles);
    double refTwoObstScore = -1436.0177004460807;
    assertEquals(refTwoObstScore, twoObstScore, 1e-7);

    // Extract obstacles and visually inspect them.
    obstacles = LaserLogic.extractObstacles(lx, ly, lt, laserAngles, laserMaxRange, obsLasers);
    System.out.println(obstacles);
  }
}
