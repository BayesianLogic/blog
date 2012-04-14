package blog.absyn;

public class Query extends Stmt {
	public Expr query;
	public Query(int p, Expr q) {
		pos = p;
		query = q;
	}
}
