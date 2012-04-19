package blog.absyn;

import blog.symbol.Symbol;

public class ParameterDec extends Dec {
	public Expr cond;
	public Symbol name;
	public Ty type;

	public ParameterDec(int p, Ty t, Symbol n, Expr c) {
		pos = p;
		type = t;
		name = n;
		cond = c;
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.sayln("ParameterDec(");
		type.printTree(pr, d + 1);
		pr.sayln(",");
		pr.indent(d + 1);
		pr.say(name.toString());
		if (cond != null) {
			pr.sayln(",");
			cond.printTree(pr, d + 1);
		}
		pr.say(")");
	}
}
