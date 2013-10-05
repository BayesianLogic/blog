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
	 * @deprecated Use {@link #printSyntax(IndentingPrinter)} instead.
	 */
	public abstract void printTree(Printer pr, int d);

	/**
	 * Set to false to disable printing of line/column information when using printSyntax.
	 * 
	 * @see #printSyntax(IndentingPrinter)
	 */
	public static boolean printSourceLocations=true;
	
	/**
	 * Prints the syntax tree, given an IndentingPrinter class.
	 * <p>
	 * In the case of syntax nodes that are mere lists of other syntax nodes (StmtList, for example),
	 * make the output prettier by printing it as a list, rather than:
	 * "(StmtList :head foo :next (StmtList :head bar :next ...) ...)"
	 * which is what calling printFields instead of printMembers would result in.
	 * 
	 * @param pr IndentingPrinter, where to print
	 * @see #printSourceLocations
	 * @see #printMembers(IndentingPrinter)
	 * @see #printFields(IndentingPrinter, Field[])
	 * @see StmtList
	 */
	public void printSyntax(IndentingPrinter pr) {
		Class myClass = this.getClass();
		pr.print('(').print(myClass.getSimpleName());
		if (printSourceLocations) {
			pr.print(" :line ").print(line);
			pr.print(" :col ").print(col);
		}
		pr.indent();
		if (this instanceof Iterable) {
			printMembers(pr);
		} else {
			// getDeclaredFields() will omit inherited fields
			printFields(pr,pruneFields(myClass.getFields()));
			//printFields(pr,pruneFields(myClass.getDeclaredFields()));
		}
								
		pr.dedent();
		pr.print(')');
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
	 * Helper for printSyntax; loops over the (pruned) fields of this.
	 * <p>
	 * In the case of syntax objects with a single "real" field, squelch the name of that field.
	 * IntExpr is a good example.
	 *  
	 * @see #printSyntax(IndentingPrinter)
	 * @see blog.absyn.IntExpr
	 */
	protected void printFields(IndentingPrinter pr, ArrayList<Field> fields) {
		if (fields.size()==1) {
			pr.print(' ');
			printValue(pr,getValue(fields.get(0)));
			return;
		}
		
		for (Field f : fields) {
			pr.print('\n');
			printField(pr, f);	
		}
	}
	
	/**
	 * Helper for printSyntax; prints out a single field in Common LISP compatible syntax.
	 * <p>
	 * Code can easily be altered to target other conventions for associations.
	 * Use <code>#:name value</code>, or <code>(name . value)</code>, for other LISPs.
	 * Use <code>name: value</code> for YAML.
	 * Use <code>"name":"value"</code> for JSON.
	 * 
	 * @param pr printer to send to
	 * @param f field to print
	 * @see #printSyntax(IndentingPrinter)
	 */
	protected void printField(IndentingPrinter pr, Field f) {
		Object name = getName(f);
		Object value = getValue(f);
		
		pr.print(":").print(name);
		pr.print(' ');printValue(pr,value);
	}
	
	/**
	 * Helper for printSyntax; prints out a single value. 
	 * <p>
	 * This method recurses through printSyntax when the value is a syntax object, otherwise it delegates to the printer.
	 * 
	 * @param pr printer to send to
	 * @param value value to print
	 * @see #printSyntax(IndentingPrinter)
	 */
	protected void printValue(IndentingPrinter pr, Object value) {
		if (value instanceof Absyn) {
			((Absyn) value).printSyntax(pr);
			return;
		}
		
		pr.print(value);	
	}
	
	/**
	 * Helper for printSyntax; prints list-implementing syntax as a bona-fide list. 
	 * <p> 
	 * Uses newlines to separate list items, under the theory that most lists will contain
	 * at least one complicated element, so, for better readability, give every element its own line.
	 * Undesirable newlines, can, of course, be gotten rid of manually.
	 * 
	 * @param pr printer to send to
	 * @param value value to print
	 * @see #printSyntax(IndentingPrinter)
	 */
	protected void printMembers(IndentingPrinter pr) {
		for (Object o : (Iterable) this) {
			pr.newline();
			printValue(pr,o);
		}
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
	 * @see #printValue(IndentingPrinter, Object)
	 */
	protected Object getValue(Field f) {
		Object name = f.getName();
		Object value = name;
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
	 * @see #printValue(IndentingPrinter, Object)
	 * @see #getValue(Field)
	 */
	protected Object getName(Field f) {
		return f.getName();
	}


	/**
	 * Uses printSyntax to deliver the S-expression rendering of the tree.
	 * 
	 * @see java.lang.Object#toString()
	 * @see #printSyntax(IndentingPrinter)
	 * @see #printSourceLocations
	 */
	public String toString() {
		ByteArrayOutputStream str = new ByteArrayOutputStream();
		IndentingPrinter pr = new IndentingPrinter(str);
		printSyntax(pr);
		return str.toString();
	}

}
