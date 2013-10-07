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

public class IndentingPrinter {

	private PrintStream out;
	private int depth=0;
	
	public IndentingPrinter reset() {
		this.depth = 0;
		this.out.flush();
		return this;
	}
    		
	public IndentingPrinter indent() {
		depth+=1;
		return this;
	}
	public IndentingPrinter dedent() {
		depth-=1;
		return this;
	}

	
	public IndentingPrinter print(char c) {
		if (c=='\n')
			newline();
		else
			out.print(c);
		return this;
	}

	public IndentingPrinter print(char[] s) {
		for (char c : s) {
			print(c);
		}
		return this;
	}

	public IndentingPrinter print(String s) {
		print(s.toCharArray());
		return this;
	}

	public IndentingPrinter print(Object obj) {
		print(String.valueOf(obj));
		return this;
	}

	public IndentingPrinter newline() {
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
	public IndentingPrinter flush() {
		out.flush();
		return this;
	}

	public void close() {
		out.close();
	}

	public boolean checkError() {
		return out.checkError();
	}

	public IndentingPrinter write(int b) {
		out.write(b);
		return this;
	}

	public IndentingPrinter print(boolean b) {
		out.print(b);
		return this;
	}

	public IndentingPrinter print(int i) {
		out.print(i);
		return this;
	}

	public IndentingPrinter print(long l) {
		out.print(l);
		return this;
	}

	public IndentingPrinter print(float f) {
		out.print(f);
		return this;
	}

	public IndentingPrinter print(double d) {
		out.print(d);
		return this;
	}

	public IndentingPrinter println(boolean x) {
		print(x);
		newline();
		return this;
	}

	public IndentingPrinter println(char x) {
		print(x);
		newline();
		return this;
	}

	public IndentingPrinter println(int x) {
		print(x);
		newline();
		return this;
	}

	public IndentingPrinter println(long x) {
		print(x);
		newline();
		return this;
	}

	public IndentingPrinter println(float x) {
		print(x);
		newline();
		return this;
	}

	public IndentingPrinter println(double x) {
		print(x);
		newline();
		return this;
	}

	public IndentingPrinter println(char[] x) {
		print(x);
		newline();
		return this;
	}

	public IndentingPrinter println(String x) {
		print(x);
		newline();
		return this;
	}

	public IndentingPrinter println(Object x) {
		print(x);
		newline();
		return this;
	}

	public IndentingPrinter printf(String format, Object... args) {
		out.printf(format, args);
		return this;
	}

	public IndentingPrinter printf(Locale l, String format, Object... args) {
		out.printf(l, format, args);
		return this;
	}

	public IndentingPrinter format(String format, Object... args) {
		out.format(format, args);
		return this;
	}
	

	public IndentingPrinter format(Locale l, String format, Object... args) {
		out.format(l, format, args);
		return this;
	}

	/*
	 * Constructors
	 */
	
	public IndentingPrinter(PrintStream out) {
		super();
		this.out = out;
	}
	public IndentingPrinter(File file) throws FileNotFoundException {
		super();
		out = new PrintStream(file);
	}
    public IndentingPrinter(OutputStream out) {
    	super();
    	this.out = new PrintStream(out);
    }
    
}
