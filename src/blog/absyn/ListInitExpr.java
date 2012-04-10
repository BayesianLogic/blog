package blog.absyn;

public class ListInitExpr extends Stmt {

	public ExprList values;

	public ListInitExpr(int p, ExprList v) {
		pos = p;
		values = v;
	}
}
