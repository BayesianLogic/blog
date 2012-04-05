package blog.common;

/**
 * The interface of a unary procedure, useful for anonymous classes used as
 * Lambda operators.
 * 
 * @author Rodrigo
 * 
 */
public interface UnaryProcedure {
	public void evaluate(Object x);
}
