package blog.absyn;

public class ExprList extends Absyn {
	public Expr head;
	public ExprList tail;

	public ExprList(Expr h, ExprList t) {
		head = h;
		tail = t;
	}
}
