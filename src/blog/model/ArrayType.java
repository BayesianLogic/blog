/**
 * 
 */
package blog.model;

/**
 * ArrayType for BLOG
 *
 * One-dimensional array of any type.
 * 
 * @author leili
 * @since Feb 11, 2014
 * 
 */
public class ArrayType extends Type {

  /**
   * @param elementType
   *          the element type for this ArrayType, e.g. Real is the element type
   *          in Real[]
   */
  public ArrayType(Type elementType) {
    super("Array<" + elementType.getName() + ">");
    this.elementType = elementType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see blog.model.Type#isSubtypeOf(blog.model.Type)
   */
  @Override
  public boolean isSubtypeOf(Type other) {
    if (other instanceof ArrayType) {
      ArrayType aty = (ArrayType) other;
      return elementType.isSubtypeOf(aty.elementType);
    }
    return false;
  }

  private Type elementType;
}
