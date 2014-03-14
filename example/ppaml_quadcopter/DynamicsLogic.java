package ppaml_quadcopter;


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
   * oldState: x, y, theta, xDot, yDot, thetaDot.
   * controls: encoderVelocity, steeringAngle.
   *
   * Outputs:
   * newState: x, y, theta, xDot, yDot, thetaDot.
   */
  public static double[] dynamics(
          double a, double b, double h, double L,
          double[] oldState,
          double encoderVelocity, double steeringAngle,
          double deltaT) {
    if (oldState.length != 6) {
      throw new IllegalArgumentException("expected 6-dimensional state vector");
    }
    double x = oldState[0];
    double y = oldState[1];
    double theta = oldState[2];
    double xDot = oldState[3];
    double yDot = oldState[4];
    double thetaDot = oldState[5];

    // Translate velocity from encoder to center of back axle:
    double velocity = encoderVelocity / (1 - Math.tan(steeringAngle) * h / L);

    // Compute new xDot, yDot, thetaDot:
    double newXDot = (
        velocity * Math.cos(theta) -
        (velocity / L) *
        (a * Math.sin(theta) + b * Math.cos(theta)) *
        Math.tan(steeringAngle));
    double newYDot = (
        velocity * Math.sin(theta) +
        (velocity / L) *
        (a * Math.cos(theta) - b * Math.sin(theta)) *
        Math.tan(steeringAngle));
    double newThetaDot = (velocity / L) * Math.tan(steeringAngle);

    // Compute new x, y, theta:
    double newX = x + deltaT * xDot;
    double newY = y + deltaT * yDot;
    double newTheta = theta + deltaT * thetaDot;
    if (newTheta > Math.PI) {
      newTheta -= 2 * Math.PI;
    } else if(newTheta < -Math.PI) {
      newTheta += 2 * Math.PI;
    }

    double[] newState = new double[6];
    newState[0] = newX;
    newState[1] = newY;
    newState[2] = newTheta;
    newState[3] = newXDot;
    newState[4] = newYDot;
    newState[5] = newThetaDot;
    return newState;
  }
};
