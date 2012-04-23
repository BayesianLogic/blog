package blog.absyn;

/**
 * @author leili
 * @date Apr 22, 2012
 */
public class QueryStmt extends Stmt {
	public Expr query;

	public QueryStmt(int p, Expr q) {
		pos = p;
		query = q;
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.sayln("Query(");
		query.printTree(pr, d + 1);
		pr.say(")");
	}
}
