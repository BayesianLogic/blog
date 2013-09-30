package blog.absyn;

import blog.symbol.Symbol;

/**
 * @author Cheng
 * @date Apr 22, 2012
 */
public class ObservationCallExpr extends Expr {
	public FuncCallExpr arg;

	public ObservationCallExpr(int p, Expr f) {
		this(0, p, f);
	}

	public ObservationCallExpr(int line, int col, Expr f) {
		super(line, col);
		arg = (FuncCallExpr)f;
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.sayln("ObservationCallExpr(");
		pr.indent(d + 1);
		pr.say(arg.toString());
		if (arg != null) {
			pr.sayln(",");
			arg.printTree(pr, d + 1);
		}
		pr.say(")");
	}
}
