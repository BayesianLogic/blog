package blog.absyn;

public class BooleanExpr extends Expr {
	public boolean value;

	public BooleanExpr(int p, boolean v) {
		pos = p;
		value = v;
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.say("BooleanExpr(");
		pr.say(value);
		pr.say(")");
	}
}
