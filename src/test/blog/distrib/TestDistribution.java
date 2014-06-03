/**
 * 
 */
package test.blog.distrib;

import java.util.LinkedList;
import java.util.List;

/**
 * Junit Test Case for Generic Distributions
 */
public class TestDistribution {
  List<Object> constructParams;
  List<Object> args;
  final double ERROR_BOUND = 1e-5;

  public TestDistribution() {
    constructParams = new LinkedList<Object>();
    args = new LinkedList<Object>();
  }

}
