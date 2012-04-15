package blog.absyn;

public class IntExpr extends Expr {
	public int value;

	public IntExpr(int p, int v) {
		pos = p;
		value = v;
	}

	@Override
	void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.say("IntExpr(");
		pr.say(value);
		pr.say(")");
	}
}
