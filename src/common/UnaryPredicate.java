package common;

/**
 * The interface of a unary predicate, useful for anonymous classes used as
 * Lambda operators.
 * 
 * @author Rodrigo
 * 
 */
public interface UnaryPredicate {
	public boolean evaluate(Object x);
}
