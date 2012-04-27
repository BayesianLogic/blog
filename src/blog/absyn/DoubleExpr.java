package blog.absyn;

/**
 * @author leili
 * @date Apr 22, 2012
 * 
 */
public class DoubleExpr extends Expr {
	public double value;

	public DoubleExpr(int p, double v) {
		this(0, p, v);
	}

	public DoubleExpr(int line, int col, double v) {
		super(line, col);
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
