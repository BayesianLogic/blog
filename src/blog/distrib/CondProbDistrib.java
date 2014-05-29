package blog.distrib;

import java.util.List;


/**
 * Common interface for all probability distributions.
 * 
 * TODO explain constructor and parameter passing
 * TODO method docs
 */
public interface CondProbDistrib {

  public void setParams(List<Object> params);

  public double getProb(Object value);

  public double getLogProb(Object value);

  public Object sampleVal();
}
