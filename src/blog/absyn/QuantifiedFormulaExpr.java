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

	@Override
	void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.sayln("QuantifiedFormulaExpr(");
		pr.indent(d + 1);
		if (quantifier == FORALL) {
			pr.sayln("FORALL,");
		} else if (quantifier == EXISTS) {
			pr.sayln("EXISTS,");
		} else
			throw new Error("Print.prExp.OpExpr");
		type.printTree(pr, d + 1);
		pr.sayln(",");
		pr.indent(d + 1);
		pr.say(var.toString());
		pr.sayln(",");
		formula.printTree(pr, d + 1);
		pr.say(")");
	}
}
