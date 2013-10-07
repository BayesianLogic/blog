package blog.absyn;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
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
