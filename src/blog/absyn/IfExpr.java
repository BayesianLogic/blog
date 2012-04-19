package blog.absyn;

public class IfExpr extends Expr {
	public Expr test;
	public Expr thenclause;
	public Expr elseclause; /* optional */

	public IfExpr(int p, Expr x, Expr y) {
		pos = p;
		test = x;
		thenclause = y;
		elseclause = null;
	}

	public IfExpr(int p, Expr x, Expr y, Expr z) {
		pos = p;
		test = x;
		thenclause = y;
		elseclause = z;
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.sayln("IfExpr(");
		test.printTree(pr, d + 1);
		pr.sayln(",");
		thenclause.printTree(pr, d + 1);
		if (elseclause != null) { /* else is optional */
			pr.sayln(",");
			elseclause.printTree(pr, d + 1);
		}
		pr.say(")");
	}
}
