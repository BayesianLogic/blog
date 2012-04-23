package blog.absyn;

/**
 * @author leili
 * @date Apr 22, 2012
 * 
 */
public class ExprList extends Absyn {
	public Expr head;
	public ExprList next;

	public ExprList(Expr h, ExprList t) {
		head = h;
		next = t;
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.say("ExprList(");
		if (head != null) {
			pr.sayln("");
			head.printTree(pr, d + 1);
			if (next != null) {
				pr.sayln(",");
				next.printTree(pr, d + 1);
			}
		}
		pr.say(")");
	}
}
