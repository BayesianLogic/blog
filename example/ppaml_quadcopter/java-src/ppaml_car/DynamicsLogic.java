package ppaml_car;


/**
 * Computes new state based on old state and controls.
 *
 * This class is deliberately decoupled from BLOG, so that we can test it
 * independently. We plug it into BLOG using the DynamicsInterp class.
 */
public class DynamicsLogic {

  /**
   * Return new state given car params, old state, controls, and deltaT.
   *
   * Inputs:
   * Car params: a, b, h, L.
   * oldState: x, y, theta
   * controls: encoderVelocity, steeringAngle.
   *
   * Outputs:
   * newState: x, y, theta
   */
  public static double[] dynamics(
          double a, double b, double h, double L,
          double[] oldState,
          double encoderVelocity, double steeringAngle,
          double deltaT) {
    if (oldState.length != 3) {
      throw new IllegalArgumentException("expected 3-dimensional state vector");
    }
    double x = oldState[0];
    double y = oldState[1];
    double theta = oldState[2];

    // Translate velocity from encoder to center of back axle:
    double velocity = encoderVelocity / (1 - Math.tan(steeringAngle) * h / L);

    // Compute xDot, yDot, thetaDot:
    double xDot = (
        velocity * Math.cos(theta) -
        (velocity / L) *
        (a * Math.sin(theta) + b * Math.cos(theta)) *
        Math.tan(steeringAngle));
    double yDot = (
        velocity * Math.sin(theta) +
        (velocity / L) *
        (a * Math.cos(theta) - b * Math.sin(theta)) *
        Math.tan(steeringAngle));
    double thetaDot = (velocity / L) * Math.tan(steeringAngle);

    // Compute new x, y, theta:
    double newX = x + deltaT * xDot;
    double newY = y + deltaT * yDot;
    double newTheta = theta + deltaT * thetaDot;
    if (newTheta > Math.PI) {
      newTheta -= 2 * Math.PI;
    } else if(newTheta < -Math.PI) {
      newTheta += 2 * Math.PI;
    }

    double[] newState = new double[3];
    newState[0] = newX;
    newState[1] = newY;
    newState[2] = newTheta;
    return newState;
  }
};
