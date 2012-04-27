package blog.absyn;

/**
 * @author leili
 * @date Apr 22, 2012
 */
public class StringExpr extends Expr {
	public String value;

	public StringExpr(int line, int col, String v) {
		super(line, col);
		value = v;
	}

	public StringExpr(int pos, String v) {
		this(0, pos, v);
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.say("StringExpr(");
		pr.say(value);
		pr.say(")");
	}
}
