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
		this.name = name;
		argTypes = NO_ARG_TYPES;
	}

	/**
	 * Creates a signature for a function with the given name and argument
	 * types.
	 */
	public FunctionSignature(String name, Type... types) {
		this.name = name;
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
		this.name = name;
		argTypes = new Type[argTypeList.size()];
		argTypeList.toArray(argTypes);
	}

	public String getName() {
		return name;
	}

	public Type[] getArgTypes() {
		return argTypes;
	}

	public boolean equals(Object o) {
		if (o instanceof FunctionSignature) {
			FunctionSignature other = (FunctionSignature) o;
			return ((name.equals(other.getName())) && Arrays.equals(argTypes,
					other.getArgTypes()));
		}
		return false;
	}

	public int hashCode() {
		int code = name.hashCode();
		for (int i = 0; i < argTypes.length; ++i) {
			code ^= argTypes[i].hashCode();
		}
		return code;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
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

	public String name;
	public Type[] argTypes;

	private static Type[] NO_ARG_TYPES = new Type[0];
}