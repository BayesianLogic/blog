/**
 * 
 */
package blog.world;

import blog.bn.DynamicCBN;
import blog.bn.DynamicCBNDiff;

/**
 * @author David T
 * @since Oct 28, 2014
 * 
 */
public class DynamicPartialWorldDiff extends PartialWorldDiff {
  /**
   * Construct a new DynamicPartialWordDiff instance taking the underlying
   * PartialWorld as input. Construct a DynamicCBNDiff instance if the
   * parameter underlying is an instance of PartialWorldDiff.
   * 
   * @param underlying
   */
  public DynamicPartialWorldDiff(PartialWorld underlying) {
    super(underlying);
    cbn = new DynamicCBNDiff(new DynamicCBN(underlying.getCBN()));
  }

  /**
   * This method is no longer used since we don't need to compute the core and
   * hence only the first constructor is needed.
   * 
   * @param underlying
   * @param toCopy
   */
  public DynamicPartialWorldDiff(PartialWorld underlying, PartialWorld toCopy) {
    super(underlying, toCopy);
  }

  private DynamicCBNDiff cbn;
}
