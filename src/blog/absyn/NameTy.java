package blog.absyn;

import blog.symbol.Symbol;

/**
 * @author leili
 * @date Apr 22, 2012
 */
public class NameTy extends Ty {
	public Symbol name;

	public NameTy(Symbol n) {
		this(0, 0, n);
	}
	
	public NameTy(int p, Symbol n) {
		this(0, p, n);
	}

	public NameTy(int line, int col, Symbol n) {
		super(line, col);
		name = n;
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.say("NameTy(");
		pr.say(name.toString());
		pr.say(")");
	}
}
