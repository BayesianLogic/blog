package blog.absyn;

public class TimeExpr extends Expr {
	public int value;

	public TimeExpr(int p, int v) {
		pos = p;
		value = v;
	}
}
