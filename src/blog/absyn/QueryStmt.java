package blog.absyn;

/**
 * @author leili
 * @date Apr 22, 2012
 */
public class QueryStmt extends Stmt {
	public Expr query;

	public QueryStmt(int line, int p, Expr q) {
		super(line, p);
		query = q;
	}

	public QueryStmt(int p, Expr q) {
		this(0, p, q);
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.sayln("Query(");
		query.printTree(pr, d + 1);
		pr.say(")");
	}
}
