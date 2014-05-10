package ppaml_car;

import java.util.List;

import blog.common.numerical.MatrixFactory;
import blog.common.numerical.MatrixLib;
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

    MatrixLib carParams = (MatrixLib) args.get(0);
    if (carParams.numRows() != 4 || carParams.numCols() != 1) {
      throw new IllegalArgumentException(
        "carParams must be a column vector of size 4");
    }
    double a = carParams.elementAt(0, 0);
    double b = carParams.elementAt(1, 0);
    double h = carParams.elementAt(2, 0);
    double L = carParams.elementAt(3, 0);

    MatrixLib oldStateTmp = (MatrixLib) args.get(1);
    if (oldStateTmp.numRows() != 6 || oldStateTmp.numCols() != 1) {
      throw new IllegalArgumentException(
        "oldState must be a column vector of size 6");
    }
    double[] oldState = new double[6];
    for (int i = 0; i < 6; i++) {
      oldState[i] = oldStateTmp.elementAt(i, 0);
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
