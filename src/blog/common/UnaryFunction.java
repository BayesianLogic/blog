package blog.common;

/**
 * The interface of a unary function, useful for anonymous classes used as
 * Lambda operators.
 * 
 * @author Rodrigo
 * 
 */
public interface UnaryFunction {
	public Object evaluate(Object x);
}
