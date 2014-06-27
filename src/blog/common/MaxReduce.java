package blog.common;

/**
 * Use this to compute the maximum element from an implicit sequence.
 * 
 * Call evaluate() on all the objects you are considering.
 * Then read the maximum element from the <code>result</code> property.
 * (The result is null if no elements were evaluated.)
 * 
 * @author cberzan
 * @since Jun 6, 2014
 */
public class MaxReduce<T extends Comparable<T>> implements UnaryProcedure {
  public T result = null;

  public void evaluate(Object x) {
    @SuppressWarnings("unchecked")
    T cand = (T) x;
    if (result == null || (cand != null && cand.compareTo(result) > 0)) {
      result = cand;
    }
  }
}
