package blog.absyn;

import blog.symbol.Symbol;

/**
 * @author leili
 * @date Apr 22, 2012
 */
public class ParameterDec extends Dec {
	public Expr cond;
	public Symbol name;
	public Ty type;

	public ParameterDec(int line, int p, Ty t, Symbol n, Expr c) {
		super(line, p);
		type = t;
		name = n;
		cond = c;
	}

	public ParameterDec(int p, Ty t, Symbol n, Expr c) {
		this(0, p, t, n, c);
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
