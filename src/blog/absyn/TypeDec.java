package blog.absyn;

import blog.symbol.Symbol;

public class TypeDec extends Dec {
	public Symbol name;

	public TypeDec(int p, Symbol n) {
		pos = p;
		name = n;
	}

	@Override
	void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.say("TypeDec(");
		pr.say(name.toString());
		pr.say(")");
	}
}
