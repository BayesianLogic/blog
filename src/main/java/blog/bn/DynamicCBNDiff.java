/**
 * 
 */
package blog.bn;

import blog.common.HashDynamicGraphDiff;

/**
 * @author David T
 * @since Oct 28, 2014
 * 
 */
public class DynamicCBNDiff extends HashDynamicGraphDiff implements CBN {
  public DynamicCBNDiff(DynamicCBN underlying) {
    super(underlying);
  }

  public DynamicCBN getUnderlying() {
    return (DynamicCBN) underlying;
  }
}
