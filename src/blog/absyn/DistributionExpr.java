package blog.absyn;

import blog.symbol.Symbol;

/**
 * @author leili
 * @date Apr 22, 2012
 * 
 */
public class DistributionExpr extends Expr {
	public Symbol name;
	public ExprList args;

	public DistributionExpr(Symbol f, ExprList a) { this(0, f, a); }
	public DistributionExpr(int p, Symbol f, ExprList a) {
		this(0, p, f, a);
	}

	public DistributionExpr(int line, int col, Symbol f, ExprList a) {
		super(line, col);
		name = f;
		args = a;
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.sayln("DistributionExpr(");
		pr.indent(d + 1);
		pr.say(name.toString());
		pr.sayln(",");
		args.printTree(pr, d + 1);
		pr.say(")");
	}
}
