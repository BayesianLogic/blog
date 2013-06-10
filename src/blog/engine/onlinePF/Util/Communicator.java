package blog.engine.onlinePF.Util;

import java.io.PrintStream;

/**
 * A convenience class, basically works like a pipedinputput/output stream pair
 * It allows a user to print strings to it
 * It also allows another user to read from it the strings that were printed
 * Actual implementations differ
 * @author cheng
 *
 */
public abstract class Communicator {
	/** the stream for printing strings to the communicator*/
	public PrintStream p;
	
	/**
	 * Prints a string to the communicator, appends the \n character
	 * @param message the string to be printed
	 */
	public abstract void printInput (String message);
	
	/**
	 * Prints a string to the communicator, does not append \n
	 * @param message the string to be printed
	 */
	public abstract void printInputNL (String message);
	
	/**
	 * Reads a line that has been printed.
	 * @return the next line that has been printed to the communicator
	 */
	public abstract String readInput ();
}
