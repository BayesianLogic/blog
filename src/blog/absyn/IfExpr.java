package blog.absyn;

import blog.symbol.Symbol;

public class IfExpr extends Expr {
	public Expr test;
	public Expr thenclause;
	public Expr elseclause; /* optional */

	public IfExpr(int p, Expr x, Expr y) {
		pos = p;
		test = x;
		thenclause = y;
		elseclause = null;
	}

	public IfExpr(int p, Expr x, Expr y, Expr z) {
		pos = p;
		test = x;
		thenclause = y;
		elseclause = z;
	}
}
