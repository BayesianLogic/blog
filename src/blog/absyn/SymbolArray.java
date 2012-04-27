package blog.absyn;

import blog.symbol.Symbol;

/**
 * @author leili
 * @date Apr 22, 2012
 */
public class SymbolArray extends Absyn {
	public Symbol name;
	public int size;

	public SymbolArray(int line, int pos, Symbol n, int s) {
		super(line, pos);
		name = n;
		size = s;
	}

	public SymbolArray(int line, int pos, Symbol n) {
		this(line, pos, n, 1);
	}

	public SymbolArray(int pos, Symbol n, int s) {
		this(0, pos, n, s);
	}

	public SymbolArray(int pos, Symbol n) {
		this(0, pos, n, 1);
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.sayln("SymbolArray(");
		pr.indent(d + 1);
		pr.say(name.toString());
		pr.sayln(",");
		pr.indent(d + 1);
		pr.say(size);
		pr.say(")");
	}
}
