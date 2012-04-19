package blog.absyn;

public class NullExpr extends Expr {
	public NullExpr(int p) {
		pos = p;
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.say("NullExpr(");
		pr.say(")");
	}
}
