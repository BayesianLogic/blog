package blog.absyn;

import blog.symbol.Symbol;

public class SymbolArrayList extends Absyn {
	public SymbolArray head;
	public SymbolArrayList next;
	
	public SymbolArrayList(int p, SymbolArray h, SymbolArrayList n) {
		pos = p;
		head = h;
		next = n;
	}
	
}
