package blog.absyn;

import blog.symbol.Symbol;

/**
 * @author leili
 * @date Apr 22, 2012
 */
public class FuncCallExpr extends Expr {
	public Symbol func;
	public ExprList args;

	public FuncCallExpr(int p, Symbol f, ExprList a) {
		this(0, p, f, a);
	}

	public FuncCallExpr(int line, int col, Symbol f, ExprList a) {
		super(line, col);
		func = f;
		args = a;
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.sayln("FuncCallExpr(");
		pr.indent(d + 1);
		pr.say(func.toString());
		if (args != null) {
			pr.sayln(",");
			args.printTree(pr, d + 1);
		}
		pr.say(")");
	}
}
