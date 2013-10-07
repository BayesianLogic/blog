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
		super();
		if (h!= null) {
			this.line = h.line;
			this.col = h.col;
			head = h;
		}
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

	/**
	 * A syntactically convenient creator of StmtList, primarily for use in testing.
	 * <p>
	 * Enable it via: 
	 * <code>
	 * import static blog.absyn.StmtList.StmtList;
	 * </code>
	 * <p>
	 * Use it via:
	 * <code>
	 * StmtList(new Stmt(...), new Stmt(...), ...);
	 * </code>
	 * 
	 * @param xs array of statements
	 * @return list of statements
	 */
	public static StmtList StmtList(Stmt... xs) {
		StmtList head = null;
		for(int i = xs.length-1; i > -1; --i)
			head = new StmtList(xs[i], head);
		return head;
	}
	

}
