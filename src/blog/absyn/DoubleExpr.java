package blog.absyn;

public class DoubleExpr extends Expr {
	public double value;

	public DoubleExpr(int p, double v) {
		pos = p;
		value = v;
	}
}
