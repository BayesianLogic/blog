package blog.absyn;

public class ExprTupleList extends Absyn {
	public Expr from;
	public Expr to;
	public ExprTupleList next;

	public ExprTupleList(Expr f, Expr t, ExprTupleList n) {
		from = f;
		to = t;
		next = n;
	}
}
