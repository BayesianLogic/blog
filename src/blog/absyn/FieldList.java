package blog.absyn;

import blog.symbol.Symbol;

/**
 * @author leili
 * @date Apr 22, 2012
 * 
 */
public class FieldList extends Absyn {
	public Symbol var;
	public Ty typ;
	public FieldList next;

	public FieldList(Symbol n, Ty t, FieldList x) {
		super(t.getLine(), t.getCol());
		var = n;
		typ = t;
		next = x;
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.sayln("FieldList(");
		typ.printTree(pr, d + 1);
		if (var != null) {
			pr.sayln(",");
			pr.indent(d + 1);
			pr.say(var.toString());
		}
		if (next != null) {
			pr.sayln(",");
			next.printTree(pr, d + 1);
		}
		pr.say(")");
	}
}
