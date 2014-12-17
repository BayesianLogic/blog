/**
 * 
 */
package blog.distrib;

import blog.common.Util;
import blog.common.numerical.MatrixLib;

/**
 * @author BEN
 * @since Dec 6, 2014
 * 
 */
public class NoisyOr implements CondProbDistrib {

  @Override
  public void setParams(Object[] params) {
    if (params.length != 2) {
      throw new IllegalArgumentException("expected two parameters");
    }
    setParams((MatrixLib) params[0], (MatrixLib) params[1]);
  }

  public void setParams(MatrixLib b, MatrixLib p) {
    if (b != null) {
      this.b = b;
      this.hasb = true;
    }
    if (p != null) {
      this.p = p;
      this.hasp = true;
    }
    if (this.hasb && this.hasp) {
      initializeConstants();
    }
  }

  private void initializeConstants() {
    this.prob = 1.0 - this.b.transpose().timesMat(this.p).det();
  }

  private void checkHasParams() {
    if (!this.hasp && !this.hasb) {
      throw new IllegalArgumentException("parameter p not provided");
    }
  }

  @Override
  public double getProb(Object value) {
    return getProb(((Boolean) value).booleanValue());
  }

  public double getProb(boolean value) {
    checkHasParams();
    if (value) {
      return this.prob;
    }
    return 1 - this.prob;
  }

  @Override
  public double getLogProb(Object value) {
    return getLogProb(((Boolean) value).booleanValue());
  }

  public double getLogProb(boolean value) {
    checkHasParams();
    if (value) {
      return Math.log(this.prob);
    }
    return Math.log(1.0 - this.prob);
  }

  @Override
  public Object sampleVal() {
    checkHasParams();
    if (Util.random() < this.prob) {
      return true;
    }
    return false;
  }

  @Override
  public Object[] getFiniteSupport() {
    return finiteSupport;
  }

  private MatrixLib b;
  private MatrixLib p;
  private double prob;
  private boolean hasb;
  private boolean hasp;
  private static final Object[] finiteSupport = { true, false };

}
