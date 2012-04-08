package blog.absyn;

public class IntExpr extends Expr {
	public int value;

	public IntExpr(int p, int v) {
		pos = p;
		value = v;
	}
}
