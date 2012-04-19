package blog.absyn;

public class DoubleExpr extends Expr {
	public double value;

	public DoubleExpr(int p, double v) {
		pos = p;
		value = v;
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.say("DoubleExpr(");
		pr.say(value);
		pr.say(")");
	}
}
