package blog.absyn;

public class ExprList extends Absyn {
	public Expr head;
	public ExprList next;

	public ExprList(Expr h, ExprList t) {
		head = h;
		next = t;
	}
}
