package blog.absyn;

import blog.symbol.Symbol;

/**
 * @author leili
 * @date Apr 22, 2012
 */
public class TypeDec extends Dec {
	public Symbol name;

	/**
	 * For dynamically created type declarations, i.e., lacking source line/column/position information 
	 * @param n the name of the declared type
	 */
	public TypeDec(Symbol n) {
		this(0, 0, n);
	}
	
	/**
	 * @param p absolute position of the first character of the type declaration in the source code for the model
	 * @param n name of the declared type
	 */
	public TypeDec(int p, Symbol n) {
		this(0, p, n);
	}

	/**
	 * 
	 * @param line where the type declaration began, in lines, should be 1-based
	 * @param pos the offset, in characters, within the line where the declaration began.  Should be 1-based.
	 * @param n name of the declared type
	 */
	public TypeDec(int line, int pos, Symbol n) {
		super(line, pos);
		name = n;
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.say("TypeDec(");
		pr.say(name.toString());
		pr.say(")");
	}
}
