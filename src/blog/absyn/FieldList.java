package blog.absyn;

import blog.symbol.Symbol;

public class FieldList extends Absyn {
	public Symbol name;
	public Ty typ;
	public FieldList tail;

	public FieldList(int p, Symbol n, Ty t, FieldList x) {
		pos = p;
		name = n;
		typ = t;
		tail = x;
	}
}
