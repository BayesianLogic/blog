/**
 * 
 */
package blog.model;

/**
 * ArrayType for BLOG
 * e.g. Real[] will be ArrayType(Type("Real"), 1);
 * e.g. Real[] will be ArrayType(Type("Real"), 2);
 * 
 * @author leili
 * @since Feb 11, 2014
 * 
 */
public class ArrayType extends Type {

  /**
   * @param elementType
   *          the element type for this ArrayType, e.g. Real in the element type
   *          in Real[]
   */
  public ArrayType(Type elementType) {
    this(elementType, 1);
  }

  /**
   * 
   * @param elementType
   *          the element type for this ArrayType, e.g. Real in the element type
   *          in Real[]
   * @param dims
   *          number of dimensions
   */
  public ArrayType(Type elementType, int dims) {
    super("Array_" + dims + "<" + elementType.getName() + ">");
    this.dims = dims;
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
      return dims == aty.dims && elementType.isSubtypeOf(aty.elementType);
    }
    return false;
  }

  private Type elementType; // element type
  private int dims; // number of dimensions, for vector, = 1; matrix = 2
}
