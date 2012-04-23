package blog.absyn;

/**
 * @author leili
 * @date Apr 22, 2012
 */
public class MapInitExpr extends Expr {

	public ExprTupleList values;

	public MapInitExpr(int p, ExprTupleList v) {
		pos = p;
		values = v;
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.sayln("MapInitExpr(");
		values.printTree(pr, d + 1);
		pr.say(")");
	}
}
