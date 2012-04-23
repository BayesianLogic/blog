package blog.absyn;

/**
 * @author leili
 * @date Apr 22, 2012
 */
public class StringExpr extends Expr {
	public String value;

	public StringExpr(int p, String v) {
		pos = p;
		value = v;
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.say("StringExpr(");
		pr.say(value);
		pr.say(")");
	}
}
