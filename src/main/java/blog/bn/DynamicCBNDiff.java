/**
 * 
 */
package blog.bn;

import blog.common.HashDynamicGraphDiff;

/**
 * @author David T
 * @since 2014Äê10ÔÂ28ÈÕ
 * 
 */
public class DynamicCBNDiff extends HashDynamicGraphDiff implements CBN {
  public DynamicCBNDiff(DynamicCBN underlying) {
    super(underlying);
  }
}
