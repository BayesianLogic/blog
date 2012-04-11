package blog.absyn;

import blog.symbol.Symbol;

public class MapTy extends Ty {
	public Ty from, to;
	public int dim;

	public MapTy(int p, Ty f, Ty t) {
		pos = p;
		from = f;
		to = t;
	}
}
