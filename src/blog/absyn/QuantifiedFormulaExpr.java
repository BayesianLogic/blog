package blog.absyn;

import blog.symbol.Symbol;

public class QuantifiedFormulaExpr extends Expr {
	public Ty type;
	public Symbol var;
	public Expr formula;
	public int quantifier;

	public QuantifiedFormulaExpr(int p, int q, Ty t, Symbol v, Expr f) {
		pos = p;
		quantifier = q;
		type = t;
		var = v;
		formula = f;
	}

	public final static int FORALL = 51, EXISTS = 52;
}
