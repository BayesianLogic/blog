package blog.absyn;

import blog.symbol.Symbol;

public class OriginFieldList extends Absyn {
	public Symbol var;
	public Symbol func;
	public OriginFieldList next;

	public OriginFieldList(int p, Symbol f, Symbol v, OriginFieldList x) {
		pos = p;
		var = v;
		func = f;
		next = x;
	}
}
