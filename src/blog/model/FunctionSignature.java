/**
 * 
 */
package blog.model;

import java.util.Arrays;
import java.util.List;

/**
 * Nested class representing a function signature, that is, a function name
 * and a tuple of argument types.
 */
public class FunctionSignature {
  /**
   * Creates a signature for a zero-ary function with the given name.
   */
  public FunctionSignature(String name) {
    this.name = name.intern();
    argTypes = NO_ARG_TYPES;
  }

  /**
   * Creates a signature for a function with the given name and argument
   * types.
   */
  public FunctionSignature(String name, Type... types) {
    this.name = name.intern();
    argTypes = types;
  }

  /**
   * Creates a signature for a function with the given name and argument
   * types.
   * 
   * @param argTypeList
   *          a List of Type objects
   */
  public FunctionSignature(String name, List<Type> argTypeList) {
    this.name = name.intern();
    argTypes = new Type[argTypeList.size()];
    argTypeList.toArray(argTypes);
  }

  public String getName() {
    return name;
  }

  public Type[] getArgTypes() {
    return argTypes;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + name.hashCode();
    result = prime * result + Arrays.hashCode(argTypes);
    return result;
  }

  /*
   * the FunctionSignature equals only when both name and types are equal
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof FunctionSignature))
      return false;
    FunctionSignature other = (FunctionSignature) obj;
    return (name == other.name) && Arrays.equals(argTypes, other.argTypes);
  }

  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append(name);
    buf.append('(');
    for (int i = 0; i < argTypes.length; ++i) {
      buf.append(argTypes[i]);
      if (i + 1 < argTypes.length) {
        buf.append(", ");
      }
    }
    buf.append(')');

    return buf.toString();
  }

  private final String name;
  private final Type[] argTypes;

  private static final Type[] NO_ARG_TYPES = new Type[0];
}
