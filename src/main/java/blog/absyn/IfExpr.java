package blog.absyn;

/**
 * @author leili
 * @date Apr 22, 2012
 */
public class IfExpr extends Expr {
	public Expr test;
	public Expr thenclause;
	public Expr elseclause; /* optional */

	public IfExpr(int p, Expr x, Expr y) {
		this(p, x, y, null);
	}

	public IfExpr(int p, Expr x, Expr y, Expr z) {
		this(0, p, x, y, z);
	}

	public IfExpr(int line, int col, Expr x, Expr y, Expr z) {
		super(line, col);
		test = x;
		thenclause = y;
		elseclause = z;
	}

	public IfExpr(int line, int col, Expr x, Expr y) {
		this(line, col, x, y, null);
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
