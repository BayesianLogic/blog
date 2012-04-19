package blog.absyn;

import blog.symbol.Symbol;

public class FieldList extends Absyn {
	public Symbol name;
	public Ty typ;
	public FieldList next;

	public FieldList(int p, Symbol n, Ty t, FieldList x) {
		pos = p;
		name = n;
		typ = t;
		next = x;
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.sayln("FieldList(");
		pr.say(name.toString());
		pr.sayln(",");
		typ.printTree(pr, d + 1);
		pr.sayln(",");
		next.printTree(pr, d + 1);
		pr.say(")");
	}
}
