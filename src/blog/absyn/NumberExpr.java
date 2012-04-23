package blog.absyn;

/**
 * @author leili
 * @date Apr 22, 2012
 */
public class NumberExpr extends Expr {
	public SetExpr values;

	public NumberExpr(int p, SetExpr s) {
		pos = p;
		values = s;
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.sayln("NumberExpr(");
		values.printTree(pr, d + 1);
		pr.say(")");
	}

}
