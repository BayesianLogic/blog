package blog.absyn;

import blog.symbol.Symbol;

public class ArrayTy extends Ty {
	public Ty typ;
	public int dim;

	public ArrayTy(int p, Ty t, int d) {
		pos = p;
		typ = t;
		dim = d;
	}
}
