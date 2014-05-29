package blog.distrib;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import blog.common.Util;

public class UnivarGaussian extends distrib.UnivarGaussian implements
    CondProbDistrib {

  public static final UnivarGaussian STANDARD = new UnivarGaussian(0, 1);

  public UnivarGaussian() {
    super(Util.getRNG());
  }

  public UnivarGaussian(double mean, double variance) {
    super(Util.getRNG());
    setParams(mean, variance);
  }

  public void setParams(List<Object> params) {
    if (params.size() != 2) {
      throw new IllegalArgumentException(
          "expected two params: mean and variance");
    }
    setParams((Double) params.get(0), (Double) params.get(1));
  }

  public double getProb(Object value) {
    return getProb((Double) value);
  }

  public double getLogProb(Object value) {
    return super.getLogProb((Double) value);
  }

  public Object sampleVal() {
    return sampleValue();
  }

  /**
   * Returns a Gaussian distribution corresponding to the product of this and
   * another Gaussian distribution.
   */
  public UnivarGaussian product(UnivarGaussian another) {
    double sumOfSigmaSquares = variance + another.variance;
    return new UnivarGaussian((mean * another.variance + another.mean
        * variance)
        / sumOfSigmaSquares, (variance * another.variance) / sumOfSigmaSquares);
  }

  /**
   * Returns the product of a set of UnivarGaussians, returning
   * <code>null</code> if set is empty.
   */
  public static UnivarGaussian product(Collection<UnivarGaussian> gaussians) {
    if (gaussians.size() == 0)
      return null;
    Iterator<UnivarGaussian> gaussiansIt = gaussians.iterator();
    UnivarGaussian result = gaussiansIt.next();
    while (gaussiansIt.hasNext()) {
      result = result.product(gaussiansIt.next());
    }
    return result;
  }

  /**
   * Returns a Gaussian representing the posterior of the mean of this Gaussian
   * (but ignoring its currently set mean) given a value of its domain.
   */
  public UnivarGaussian meanPosterior(double value) {
    return new UnivarGaussian(value, variance);
  }
}
