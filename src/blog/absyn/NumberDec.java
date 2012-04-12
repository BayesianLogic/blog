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
}
