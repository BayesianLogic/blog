package blog.absyn;

public class BooleanExpr extends Expr {
	public boolean value;

	public BooleanExpr(int p, boolean v) {
		pos = p;
		value = v;
	}
}
