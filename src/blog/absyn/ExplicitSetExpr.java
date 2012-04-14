package blog.absyn;

public class ExplicitSetExpr extends SetExpr {

	public ExprList values;

	public ExplicitSetExpr(int p, ExprList v) {
		pos = p;
		values = v;
	}
}
