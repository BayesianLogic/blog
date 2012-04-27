package blog.absyn;

/**
 * Time expression
 * 
 * @author leili
 * @date Apr 22, 2012
 * @deprecated please use {@link OpExpr} instead
 * @see OpExpr
 */
public class TimeExpr extends Expr {
	public Expr value;

	/**
	 * @deprecated
	 * @param p
	 * @param v
	 */
	public TimeExpr(int p, Expr v) {
		this(0, p, v);
	}

	public TimeExpr(int line, int col, Expr v) {
		super(line, col);
		value = v;
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.sayln("TimeExpr(");
		value.printTree(pr, d + 1);
		pr.say(")");
	}
}
