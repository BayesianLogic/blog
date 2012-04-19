package blog.absyn;

import blog.symbol.Symbol;

public class ImplicitSetExpr extends SetExpr {

	public Symbol var;
	public Ty typ;
	public Expr cond;

	public ImplicitSetExpr(int p, Ty t, Symbol v, Expr c) {
		pos = p;
		var = v;
		typ = t;
		cond = c;
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.sayln("ImplicitSetExpr(");
		typ.printTree(pr, d + 1);
		if (var != null) {
			pr.sayln(",");
			pr.indent(d + 1);
			pr.say(var.toString());
		}
		if (cond != null) {
			pr.sayln(",");
			cond.printTree(pr, d + 1);
		}
		pr.say(")");
	}
}
