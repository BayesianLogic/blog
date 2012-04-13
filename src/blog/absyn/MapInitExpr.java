package blog.absyn;

public class MapInitExpr extends Expr {

	public ExprTupleList values;

	public MapInitExpr(int p, ExprTupleList v) {
		pos = p;
		values = v;
	}
}
