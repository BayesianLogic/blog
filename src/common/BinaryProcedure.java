package common;

/**
 * The interface of a binary procedure, useful for anonymous classes used as
 * Lambda operators.
 * 
 * @author Rodrigo
 * 
 */
public interface BinaryProcedure {
	public void evaluate(Object x, Object y);
}
