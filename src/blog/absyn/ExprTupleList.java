package blog.absyn;

/**
 * @author leili
 * @date Apr 22, 2012
 * 
 */
public class ExprTupleList extends Absyn {
	public Expr from;
	public Expr to;
	public ExprTupleList next;

	public ExprTupleList(Expr f, Expr t, ExprTupleList n) {
		from = f;
		to = t;
		next = n;
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.say("ExprTupleList(");
		if (from != null) {
			pr.sayln("");
			from.printTree(pr, d + 1);
			pr.sayln(",");
			if (to != null) {
				to.printTree(pr, d + 1);
			}
			if (next != null) {
				pr.sayln(",");
				next.printTree(pr, d + 1);
			}
		}
		pr.say(")");
	}
}
