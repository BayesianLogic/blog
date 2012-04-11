package blog.absyn;

public class MapInitExpr extends Stmt {

	public ExprTupleList values;

	public MapInitExpr(int p, ExprTupleList v) {
		pos = p;
		values = v;
	}
}
