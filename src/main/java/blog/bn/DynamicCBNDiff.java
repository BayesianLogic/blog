/**
 * 
 */
package blog.bn;

import blog.common.HashDynamicGraphDiff;

/**
 * @author David T
 * @since 2014��10��28��
 * 
 */
public class DynamicCBNDiff extends HashDynamicGraphDiff implements CBN {
  public DynamicCBNDiff(DynamicCBN underlying) {
    super(underlying);
  }
}
