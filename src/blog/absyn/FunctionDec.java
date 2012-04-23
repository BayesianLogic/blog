package blog.absyn;

import blog.symbol.Symbol;

/**
 * @author leili
 * @date Apr 22, 2012
 */
public class FunctionDec extends Dec {
	public Symbol name;
	public FieldList params;
	public Ty result;
	public Expr body;

	public FunctionDec(int p, Symbol n, FieldList a, Ty r, Expr b) {
		pos = p;
		name = n;
		params = a;
		result = r;
		body = b;
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.sayln("FunctionDec(");
		result.printTree(pr, d + 1);
		pr.sayln(",");
		pr.indent(d + 1);
		pr.say(name.toString());
		if (params != null) {
			pr.sayln(",");
			params.printTree(pr, d + 1);
		}
		if (body != null) {
			pr.sayln(",");
			body.printTree(pr, d + 1);
		}
		pr.say(")");
	}
}
