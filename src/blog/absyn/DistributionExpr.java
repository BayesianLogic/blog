package blog.absyn;

import blog.symbol.Symbol;

public class DistributionExpr extends Expr {
	public Symbol name;
	public ExprList args;

	public DistributionExpr(int p, Symbol f, ExprList a) {
		pos = p;
		name = f;
		args = a;
	}
}
