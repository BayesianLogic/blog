package blog.absyn;

/**
 * @author leili
 * @date Apr 22, 2012
 * 
 */
public class ExplicitSetExpr extends SetExpr {

	public ExprList values;

	public ExplicitSetExpr(int p, ExprList v) {
		this(0, p, v);
	}

	public ExplicitSetExpr(int line, int col, ExprList v) {
		super(line, col);
		values = v;
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.sayln("ExplicitSetExpr(");
		values.printTree(pr, d + 1);
		pr.say(")");
	}
}
