package blog.absyn;

import blog.symbol.Symbol;

public class SymbolArray extends Absyn {
	public Symbol name;
	public int size;
	
	public SymbolArray(int pos, Symbol n, int s) {
		name = n;
		size = s;
	}
	
	public SymbolArray(int pos, Symbol n) {
		this(pos, n, 1);
	}
}
