package blog.absyn;

public class Query extends Stmt {
	public Expr query;

	public Query(int p, Expr q) {
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
