package ppaml_quadcopter;

import java.util.List;

import blog.common.numerical.MatrixFactory;
import blog.model.AbstractFunctionInterp;


public class DynamicsInterp extends AbstractFunctionInterp {

  public DynamicsInterp(List objs) {
  }

  /**
   * Compute dynamics update.
   *
   * Input: carParams, oldState, velocity, steering, deltaT.
   * Output: newState.
   */
  public Object getValue(List args) {
    if (args.size() != 5) {
      throw new IllegalArgumentException("DynamicsInterp expected 5 arguments");
    }

    double[] carParams = (double[]) args.get(0);
    if (carParams.length != 4) {
      throw new IllegalArgumentException("carParams must have 4 values");
    }
    double a = carParams[0];
    double b = carParams[1];
    double h = carParams[2];
    double L = carParams[4];

    double[] oldState = (double[]) args.get(1);
    if (oldState.length != 6) {
      throw new IllegalArgumentException("oldState must have 6 values");
    }

    double velocity = (double) args.get(2);
    double steering = (double) args.get(3);
    double deltaT = (double) args.get(4);

    double[] newState = DynamicsLogic.dynamics(
      a, b, h, L, oldState, velocity, steering, deltaT);

    // Convert to MatrixLib.
    // Additional step required because MatrixLib only takes a double[][].
    double[][] tmp = new double[newState.length][1];
    for (int i = 0; i < newState.length; i++) {
      tmp[i][0] = newState[i];
    }
    return MatrixFactory.fromArray(tmp);
  }
}
