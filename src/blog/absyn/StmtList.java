package blog.absyn;

public class StmtList extends Absyn {
	public Stmt head;
	public StmtList tail;

	public StmtList(Stmt h, StmtList t) {
		head = h;
		tail = t;
	}
}
