package blog.absyn;

/**
 * @author leili
 * @date Apr 22, 2012
 */
public class ListInitExpr extends Expr {

	public ExprList values;
	public Ty type;

	public ListInitExpr(int p, ExprList v) {
		this(0, p, v);
	}

	public ListInitExpr(int line, int col, ExprList v) {
		super(line, col);
		values = v;
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.sayln("ListInitExpr(");
		values.printTree(pr, d + 1);
		pr.say(")");
	}
}
