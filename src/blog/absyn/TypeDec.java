package blog.absyn;

import blog.symbol.Symbol;

/**
 * @author leili
 * @date Apr 22, 2012
 */
public class TypeDec extends Dec {
	public Symbol name;

	/**
	 * @param p
	 * @param n
	 */
	public TypeDec(int p, Symbol n) {
		this(0, p, n);
	}

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
