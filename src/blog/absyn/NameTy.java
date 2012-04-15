package blog.absyn;

import blog.symbol.Symbol;

public class NameTy extends Ty {
	public Symbol name;

	public NameTy(int p, Symbol n) {
		pos = p;
		name = n;
	}

	@Override
	void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.say("NameTy(");
		pr.say(name.toString());
		pr.say(")");
	}
}
