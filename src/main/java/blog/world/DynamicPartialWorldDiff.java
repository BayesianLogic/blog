/**
 * 
 */
package blog.world;

/**
 * @author David T
 * @since Oct 28, 2014
 * 
 */
public class DynamicPartialWorldDiff extends PartialWorldDiff {
  public DynamicPartialWorldDiff(PartialWorld underlying) {
    super(underlying);
  }

  public DynamicPartialWorldDiff(PartialWorld underlying, PartialWorld toCopy) {
    super(underlying, toCopy);
  }
}
