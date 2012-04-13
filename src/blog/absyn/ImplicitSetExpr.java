package blog.absyn;

import blog.symbol.Symbol;

public class ImplicitSetExpr extends Expr {

	public Symbol var;
	public Ty type;
	public Expr cond;

	public ImplicitSetExpr(int p, Ty t, Symbol v, Expr c) {
		pos = p;
		var = v;
		type = t;
		cond = c;
	}
}
