package blog.distrib;

import java.util.List;

/**
 * Common interface for all probability distributions.
 * 
 * TODO explain constructor and parameter passing
 * TODO method docs
 * TODO childType in sampleVal??
 */
public interface Distrib {

  void setParams(List<Object> params);

  double getProb(Object value);

  double getLogProb(Object value);

  Object sampleVal();
}
