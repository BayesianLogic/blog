package blog.absyn;

import blog.symbol.Symbol;

public class NumberDec extends Dec {
	public Symbol name;
	public OriginFieldList params;
	public Expr body;

	public NumberDec(int p, Symbol n, OriginFieldList a, Expr b) {
		pos = p;
		name = n;
		params = a;
		body = b;
	}

	@Override
	void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.sayln("NumberDec(");
		pr.indent(d + 1);
		pr.say(name.toString());
		if (params != null) {
			pr.sayln(",");
			params.printTree(pr, d + 1);
		}
		pr.sayln(",");
		body.printTree(pr, d + 1);
	}
}
