package blog.absyn;

public class ListInitExpr extends Expr {

	public ExprList values;

	public ListInitExpr(int p, ExprList v) {
		pos = p;
		values = v;
	}
}
