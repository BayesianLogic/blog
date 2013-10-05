package blog.absyn;

/**
 * @author leili
 * @date Apr 22, 2012
 */
public class SymbolArrayList extends Absyn implements Iterable<SymbolArray> {
	public SymbolArray head;
	public SymbolArrayList next;

	public SymbolArrayList(SymbolArray h, SymbolArrayList n) {
		super(h.getLine(), h.getCol());
		head = h;
		next = n;
	}

	@Override
	public void printTree(Printer pr, int d) {
		if (head != null) {
			head.printTree(pr, d);
			pr.sayln("");
			if (next != null)
				next.printTree(pr, d);
		}
	}
	
	/**
	 * @see StmtList.Iterator
	 */
	public class Iterator implements java.util.Iterator<SymbolArray> {
		SymbolArrayList curr=null;
		public Iterator(SymbolArrayList SymbolArrayList) { curr = SymbolArrayList; }
		public boolean hasNext() { return curr != null; }
		public SymbolArray next() {
			SymbolArray o = curr.head;
			curr = curr.next;
			return o;
		}
		public void remove() { throw new UnsupportedOperationException(); }
	}
	public Iterator iterator() { return new Iterator(this);}

}
