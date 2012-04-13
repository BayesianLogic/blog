package blog.absyn;

import blog.symbol.Symbol;

public class ParameterDec extends Dec {
	public Expr cond;
	public Symbol name;
	public Ty type;

	public ParameterDec(int p, Ty t, Symbol n, Expr c) {
		pos = p;
		type = t;
		name = n;
		cond = c;
	}
}
