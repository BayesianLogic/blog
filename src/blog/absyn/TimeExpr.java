package blog.absyn;

/**
 * @author leili
 * @date Apr 22, 2012
 */
public class TimeExpr extends Expr {
	public int value;

	public TimeExpr(int p, int v) {
		pos = p;
		value = v;
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.say("TimeExpr(");
		pr.say(value);
		pr.say(")");
	}
}
