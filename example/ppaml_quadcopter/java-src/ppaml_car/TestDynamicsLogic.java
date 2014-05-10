package ppaml_car;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public class TestDynamicsLogic {

  @Test
  public void testReadingsForObstacle() {
    double a = 0.299541;
    double b = 0.0500507;
    double h = 0;
    double L = 0.257717;
    double[] oldState = new double[]{1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
    double velocity = 0.5;
    double steering = 0.2;
    double deltaT = 0.1;
    double[] referenceNewState = new double[]{
      1.4, 2.5, -2.683185307179586,
      -0.4921337226266671, -0.048842448876763131, 0.39328029487513927};
    double newState[] = DynamicsLogic.dynamics(
      a, b, h, L, oldState, velocity, steering, deltaT);
    assertEquals(oldState.length, referenceNewState.length);
    assertEquals(newState.length, referenceNewState.length);
    for (int i = 0; i < newState.length; i++) {
      assertEquals(newState[i], referenceNewState[i], 1e-7);
    }
  }
}
