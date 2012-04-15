package blog.absyn;

public class StringExpr extends Expr {
	public String value;

	public StringExpr(int p, String v) {
		pos = p;
		value = v;
	}

	@Override
	void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.say("StringExpr(");
		pr.say(value);
		pr.say(")");
	}
}
