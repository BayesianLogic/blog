package blog.absyn;

public class SymbolArrayList extends Absyn {
	public SymbolArray head;
	public SymbolArrayList next;

	public SymbolArrayList(int p, SymbolArray h, SymbolArrayList n) {
		pos = p;
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
