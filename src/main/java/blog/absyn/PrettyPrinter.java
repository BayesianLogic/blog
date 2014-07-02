package blog.absyn;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Locale;

/**
 * @author William Cushing
 * @date 2013/10/4
 */

public class PrettyPrinter {

	private PrintStream out;
	private int depth=0;
	
	/**
	 * Set to false to disable printing of line/column information when using printSyntax.
	 * 
	 * @see #printSyntax(PrettyPrinter)
	 */
	public boolean printSourceLocations=true;
	
	public PrettyPrinter reset() {
		this.depth = 0;
		this.out.flush();
		return this;
	}
    		
	public PrettyPrinter indent() {
		depth+=1;
		return this;
	}
	public PrettyPrinter dedent() {
		depth-=1;
		return this;
	}

	
	public PrettyPrinter print(char c) {
		if (c=='\n')
			newline();
		else
			out.print(c);
		return this;
	}

	public PrettyPrinter print(char[] s) {
		for (char c : s) {
			print(c);
		}
		return this;
	}

	public PrettyPrinter print(String s) {
		print(s.toCharArray());
		return this;
	}

	public PrettyPrinter print(Object obj) {
		print(String.valueOf(obj));
		return this;
	}

	public PrettyPrinter newline() {
		out.println();
		for(int i=0; i < depth;++i) {
			out.print('\t');
		}
		return this;
	}

	/*
	 * Delegated Methods 
	 * 
	 * These are essentially auto-generated, but some of the void return types
	 * were altered to return this instead.
	 * 
	 */
	public PrettyPrinter flush() {
		out.flush();
		return this;
	}

	public void close() {
		out.close();
	}

	public boolean checkError() {
		return out.checkError();
	}

	public PrettyPrinter write(int b) {
		out.write(b);
		return this;
	}

	public PrettyPrinter print(boolean b) {
		out.print(b);
		return this;
	}

	public PrettyPrinter print(int i) {
		out.print(i);
		return this;
	}

	public PrettyPrinter print(long l) {
		out.print(l);
		return this;
	}

	public PrettyPrinter print(float f) {
		out.print(f);
		return this;
	}

	public PrettyPrinter print(double d) {
		out.print(d);
		return this;
	}

	public PrettyPrinter println(boolean x) {
		print(x);
		newline();
		return this;
	}

	public PrettyPrinter println(char x) {
		print(x);
		newline();
		return this;
	}

	public PrettyPrinter println(int x) {
		print(x);
		newline();
		return this;
	}

	public PrettyPrinter println(long x) {
		print(x);
		newline();
		return this;
	}

	public PrettyPrinter println(float x) {
		print(x);
		newline();
		return this;
	}

	public PrettyPrinter println(double x) {
		print(x);
		newline();
		return this;
	}

	public PrettyPrinter println(char[] x) {
		print(x);
		newline();
		return this;
	}

	public PrettyPrinter println(String x) {
		print(x);
		newline();
		return this;
	}

	public PrettyPrinter println(Object x) {
		print(x);
		newline();
		return this;
	}

	public PrettyPrinter printf(String format, Object... args) {
		out.printf(format, args);
		return this;
	}

	public PrettyPrinter printf(Locale l, String format, Object... args) {
		out.printf(l, format, args);
		return this;
	}

	public PrettyPrinter format(String format, Object... args) {
		out.format(format, args);
		return this;
	}
	

	public PrettyPrinter format(Locale l, String format, Object... args) {
		out.format(l, format, args);
		return this;
	}

	
	/* Common LISP style pretty printing */
	
	/**
	 * Prints out abstract syntax, "prettily".
	 * <p>
	 * In the case of syntax nodes that are mere lists of other syntax nodes (StmtList, for example),
	 * make the output prettier by printing it as a list, rather than:
	 * "(StmtList :head foo :next (StmtList :head bar :next ...) ...)"
	 * which is what calling printFields instead of printMembers would result in.
	 * 
	 * @param absyn syntax to print
	 * @see PrettyPrinter#printSourceLocations
	 * @see #printMembers(Absyn)
	 * @see #printFields(Absyn, ArrayList)
	 * @see StmtList
	 */
	public void printSyntax(Absyn absyn) {
		Class myClass = absyn.getClass();
		print('(').print(myClass.getSimpleName());
		if (printSourceLocations) {
			print(' ');
			printField(absyn,"line",absyn.line);
			print(' ');
			printField(absyn,"col",absyn.col);
		}
		indent();
		if (absyn instanceof Iterable) {
			printMembers(absyn);
		} else {
			// getDeclaredFields() will omit inherited fields
			printFields(absyn,Absyn.pruneFields(myClass.getFields()));
			//printFields(pr,pruneFields(myClass.getDeclaredFields()));
		}
								
		dedent();
		print(')');
	}

	/**
	 * Helper for printSyntax; loops over the (pruned) fields of this.
	 * <p>
	 * In the case of syntax objects with a single "real" field, squelch the name of that field.
	 * IntExpr is a good example.
	 *  
	 * @param absyn TODO
	 * @param fields TODO
	 * @see #printSyntax(PrettyPrinter)
	 * @see blog.absyn.IntExpr
	 */
	protected void printFields(Absyn absyn, ArrayList<Field> fields) {
		if (fields.size()==1) {
			print(' ');
			printValue(absyn.getValue(fields.get(0)));
			return;
		}
		
		for (Field f : fields) {
			print('\n');
			printField(absyn, f);	
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
	 * @param absyn TODO
	 * @param f field to print
	 * @see #printSyntax(PrettyPrinter)
	 */
	protected void printField(Absyn absyn, Field f) {
		String name = absyn.getName(f);
		Object value = absyn.getValue(f);
	
		printField(absyn,name,value);
	}

	/**
	 * @param absyn TODO
	 * @param name TODO
	 * @param value TODO
	 * @see #MISSING()
	 */
	protected void printField(Absyn absyn, String name, Object value) {
		print(":").print(name);
		print(' ');printValue(value);
	}

	/**
	 * Helper for printSyntax; prints out a single value. 
	 * <p>
	 * This method recurses through printSyntax when the value is a syntax object, otherwise it delegates to the printer.
	 * 
	 * @param value value to print
	 * @see #printSyntax(PrettyPrinter)
	 */
	protected void printValue(Object value) {
		if (value instanceof Absyn) {
			printSyntax((Absyn)value);
			return;
		}
		
		print(value);	
	}

	/**
	 * Helper for printSyntax; prints list-implementing syntax as a bona-fide list. 
	 * <p> 
	 * Uses newlines to separate list items, under the theory that most lists will contain
	 * at least one complicated element, so, for better readability, give every element its own line.
	 * Undesirable newlines, can, of course, be gotten rid of manually.
	 * 
	 * @param absyn list-implementing syntax to print
	 * @see #printSyntax(PrettyPrinter)
	 */
	protected void printMembers(Absyn absyn) {
		for (Object o : (Iterable) absyn) {
			newline();
			printValue(o);
		}
	}

	/*
	 * Constructors
	 */	
	public PrettyPrinter(PrintStream out) {
		super();
		this.out = out;
	}
	public PrettyPrinter(File file) throws FileNotFoundException {
		super();
		out = new PrintStream(file);
	}
    public PrettyPrinter(OutputStream out) {
    	super();
    	this.out = new PrintStream(out);
    }
    
}
