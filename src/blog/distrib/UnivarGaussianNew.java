package blog.distrib;

import java.util.List;

import blog.common.Util;

public class UnivarGaussianNew extends distrib.UnivarGaussian implements
    Distrib {

  public UnivarGaussianNew() {
    super(Util.getRNG());
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
    return getLogProb((Double) value);
  }

  public Object sampleVal() {
    return sampleValue();
  }
}
