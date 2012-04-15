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

	@Override
	void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.sayln("FuncCallExpr(");
		pr.indent(d + 1);
		pr.say(func.toString());
		pr.sayln(",");
		args.printTree(pr, d + 1);
		pr.say(")");
	}
}
