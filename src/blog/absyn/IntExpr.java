package blog.absyn;

/**
 * @author leili
 * @date Apr 22, 2012
 */
public class IntExpr extends Expr {
	public int value;

	public IntExpr(int p, int v) {
		pos = p;
		value = v;
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.say("IntExpr(");
		pr.say(value);
		pr.say(")");
	}
}
