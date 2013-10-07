package blog.absyn;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

/**
 * Root class for abstract syntax tree, all nodes in the syntax tree should be
 * sub-type of this one.
 * 
 * The representation is independent of underlying engine and sampling method.
 * And is also independent of the intermediate representation of the model,
 * which including data structures for computing/sampling.
 * 
 * @author leili
 * @date Apr 22, 2012
 * 
 */

abstract public class Absyn {
	public int col;
	public int line;

	/**
	 * @param line
	 * @param col
	 */
	public Absyn(int line, int col) {
		this.line = line;
		this.col = col;
	}

	/**
	 * @deprecated
	 */
	public Absyn() {
		line = 0;
		col = 0;
	}

	/**
	 * @deprecated
	 * @param p
	 */
	public Absyn(int p) {
		this(0, p);
	}

	/**
	 * @return position of this abstract syntax tree in the input file
	 */
	public int getCol() {
		return col;
	}

	/**
	 * should directly use .line
	 * 
	 * @return
	 */
	public int getLine() {
		return line;
	}

	/**
	 * Prints the syntax tree, given a Printer class.
	 * 
	 * @param pr Printer, where to print
	 * @param d depth of the node in the tree
	 * 
	 * @deprecated Use {@link #MISSING()} instead.  In a child, consider deleting/deprecating printTree.
	 */
	public void printTree(Printer pr, int d) { 
		throw new UnsupportedOperationException();
	}

	/**
	 * Filters out the source location fields, as well as class-level fields.
	 * 
	 * @param fields result of getFields() 
	 * 	(or getDeclaredFields())
	 * @return new ArrayList omitting "line", "col", and static fields
	 * @see Class#getFields()
	 * @see Class#getDeclaredFields()
	 * @see Modifier
	 * @see #line
	 * @see #col
	 */
	protected static ArrayList<Field> pruneFields(Field[] fields) {
		ArrayList<Field> ret = new ArrayList<Field>(fields.length-2);
		for (Field f : fields) {
			if (shouldPrune(f)) continue;
			ret.add(f);
		}
		return ret;
	}

	/**
	 * Helper for pruneFields; tests to see whether a field ought to be pruned.
	 * 
	 * "Ought to be pruned" means that the field tracks source locations,
	 *  or that the "field" is a class-level, rather than instance-level, field.
	 *  
	 * @param f field to test
	 * @return true if it should be squelched
	 * @see #pruneFields(Field[])
	 */
	protected static boolean shouldPrune(Field f) {
		if (f.getName() == "line") return true;
		if (f.getName() == "col") return true;
		if (Modifier.isStatic(f.getModifiers())) return true;
		return false;
	}

	/**
	 * Helper for printValue; looks up the value of a field reflectively, squelches any exceptions.
	 * <p>
	 * There shouldn't be any exceptions generated, as long as this method is called with a Field object in fact representing a Field of this object.
	 * The method is protected (private might be better) to help ensure that it is called correctly.
	 * 
	 * @param f a Field object representing some field of this object
	 * @return its value
	 * @see java.lang.reflect.Field
	 * @see #MISSING()
	 */
	protected Object getValue(Field f) {
		Object value = null;
		try {
			value = f.get(this);
		} catch (IllegalArgumentException e) {
			// "can't" happen
		} catch (IllegalAccessException e) {
			// "can't" happen
		}
		return value;
	}
	/**
	 * Helper for printValue; for parsimony with getValue: returns the name of a given field.
	 * 
	 * @param f a Field reflectively representing a field of this object
	 * @return its name
	 * @see java.lang.reflect.Field
	 * @see #MISSING()
	 * @see #getValue(Field)
	 */
	protected String getName(Field f) {
		return f.getName();
	}


	/**
	 * Uses printSyntax to deliver the S-expression rendering of the tree.
	 * 
	 * @see java.lang.Object#toString()
	 * @see #MISSING()
	 * @see PrettyPrinter#printSourceLocations
	 */
	public String toString() {
		ByteArrayOutputStream str = new ByteArrayOutputStream();
		PrettyPrinter pr = new PrettyPrinter(str);
		pr.printSyntax(this);
		return str.toString();
	}

}
