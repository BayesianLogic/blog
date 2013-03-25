package blog.engine.onlinePF.absyn;


public class StmtList extends Absyn {
	public Stmt head;
	public StmtList next;

	public StmtList(Stmt h, StmtList t) {
		super(0,0);
        if (h != null) {
            this.line = h.getLine(); 
            this.col = h.getCol();
        }
		head = h;
		next = t;
	}


}
