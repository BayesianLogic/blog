package blog.absyn;

/**
 * @author leili
 * @date Apr 22, 2012
 */
public class SymbolArrayList extends Absyn {
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

}
