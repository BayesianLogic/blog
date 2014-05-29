/**
 * 
 */
package blog.old_distrib;

import java.util.List;

import blog.objgen.ObjectSet;

/**
 * @author leili
 * @since Dec 12, 2013
 * 
 */

public class Size extends AbstractCondProbDistrib {
  /**
   * Creates a Size CPD, returns the size of the underlying set
   */
  public Size() {
  }

  /**
   * Creates a Size CPD. The CPD takes no parameters.
   * 
   * @throws IllegalArgumentException
   *           if <code>params</code> is non-empty
   */
  public Size(List params) {
    if (!params.isEmpty()) {
      throw new IllegalArgumentException(
          "Size CPD does not take any parameters.");
    }
  }

  /**
   * Takes a single argument, namely a set S, Return 1
   * if <code>value</code> is the same as the size of S, and otherwise 0.
   * 
   * @throws IllegalArgumentException
   *           if <code>args</code> contains anything other than a single
   *           argument of class ObjectSet.
   */
  public double getProb(List args, Object value) {
    ObjectSet s = processArgs(args);
    Integer n = new Integer(0);
    if (!s.isEmpty()) {
      n = new Integer(s.size());
    }
    if (n.equals(value))
      return 1;
    else
      return 0;
  }

  /**
   * Takes a single argument, namely a finite set S. Returns the size of S
   * 
   * @throws IllegalArgumentException
   *           if <code>args</code> contains anything other than a single
   *           argument of class ObjectSet.
   */
  public Object sampleVal(List args) {
    ObjectSet s = processArgs(args);
    if (s.isEmpty()) {
      return new Integer(0);
    }

    return new Integer(s.size());
  }

  private ObjectSet processArgs(List args) {
    if (args.size() != 1) {
      throw new IllegalArgumentException("Size CPD takes exactly one argument.");
    }
    if (!(args.get(0) instanceof ObjectSet)) {
      throw new IllegalArgumentException(
          "Size CPD takes an argument of class ObjectSet, " + "not one of "
              + args.get(0).getClass() + ".");
    }

    return (ObjectSet) args.get(0);
  }
}
