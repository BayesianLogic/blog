package blog.absyn;

import blog.symbol.Symbol;

public class NumberDec extends Dec {
	public Ty type;
	public OriginFieldList params;
	public Expr body;

	public NumberDec(int p, Ty n, OriginFieldList a, Expr b) {
		pos = p;
		type = n;
		params = a;
		body = b;
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.sayln("NumberDec(");
		type.printTree(pr, d + 1);
		if (params != null) {
			pr.sayln(",");
			params.printTree(pr, d + 1);
		}
		pr.sayln(",");
		body.printTree(pr, d + 1);
	}
}
