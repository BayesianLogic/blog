package blog.absyn;

public class StmtList extends Absyn {
	public Stmt head;
	public StmtList next;

	public StmtList(Stmt h, StmtList t) {
		head = h;
		next = t;
	}

	@Override
	void printTree(Printer pr, int d) {
		if (head != null) {
			head.printTree(pr, d);
			pr.sayln("");
			if (next != null) {
				next.printTree(pr, d);
			}
		}
	}

}
