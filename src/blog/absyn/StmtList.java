package blog.absyn;

/**
 * @author leili
 * @date Apr 22, 2012
 */
public class StmtList extends Absyn {
	public Stmt head;
	public StmtList next;

	public StmtList(Stmt h, StmtList t) {
        if (h != null) {
            this.line = h.getLine(); 
            this.col = h.getCol();
        }
		head = h;
		next = t;
	}

	@Override
	public void printTree(Printer pr, int d) {
		if (head != null) {
			head.printTree(pr, d);
			pr.sayln("");
			if (next != null) {
				next.printTree(pr, d);
			}
		}
	}

}
