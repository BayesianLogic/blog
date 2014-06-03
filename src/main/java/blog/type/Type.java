/**
 * 
 */
package blog.type;

/**
 * @author leili
 * @date Apr 16, 2012
 * 
 */
public abstract class Type {
	public Type actual() {
		return this;
	}

	public boolean coerceTo(Type t) {
		return false;
	}
}
