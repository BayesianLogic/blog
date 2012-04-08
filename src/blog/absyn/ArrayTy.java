package blog.absyn;

import blog.symbol.Symbol;

public class ArrayTy extends Ty {
	public Symbol typ;
	public int dim;

	public ArrayTy(int p, Symbol t, int d) {
		pos = p;
		typ = t;
		dim = d;
	}
}
