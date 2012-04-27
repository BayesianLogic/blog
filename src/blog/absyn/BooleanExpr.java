package blog.absyn;

/**
 * @author leili
 * @date Apr 22, 2012
 * 
 */
public class BooleanExpr extends Expr {
	public boolean value;

	public BooleanExpr(int p, boolean v) {
		this(0, p, v);
	}

	public BooleanExpr(int line, int col, boolean v) {
		super(line, col);
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
