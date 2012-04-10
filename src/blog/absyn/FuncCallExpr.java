package blog.absyn;

import blog.symbol.Symbol;

public class FuncCallExpr extends Expr {
	public Symbol func;
	public ExprList args;

	public FuncCallExpr(int p, Symbol f, ExprList a) {
		pos = p;
		func = f;
		args = a;
	}
}
