package blog.absyn;

import java.util.Iterator;

/**
 * Represents a list of BLOG statements.
 * 
 * @author leili
 * @author William Cushing
 * @date Apr 22, 2012
 * @modified 2013/10/4
 * @see Stmt
 * @see Absyn
 */
public class StmtList extends Absyn implements Iterable<Stmt> {
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

	/**
	 * A compact implementation of Iterator, allowing "foreach" syntax:<p>
	 *  <code>for(Stmt s : this) { ... }</code>.
	 * <p>
	 * Copy-paste into other syntax lists, replacing "Stmt" with, for example, "Expr".
	 *
	 * @see java.util.Iterator
	 */
	public class Iterator implements java.util.Iterator<Stmt> {
		StmtList curr=null;
		public Iterator(StmtList stmtList) { curr = stmtList; }
		public boolean hasNext() { return curr != null; }
		public Stmt next() {
			Stmt o = curr.head;
			curr = curr.next;
			return o;
		}
		public void remove() { throw new UnsupportedOperationException(); }
	}
	public Iterator iterator() { return new Iterator(this);}

}
