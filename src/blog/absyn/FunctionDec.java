package blog.absyn;

import blog.symbol.Symbol;

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
}
