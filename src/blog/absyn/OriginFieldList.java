package blog.absyn;

import blog.symbol.Symbol;

/**
 * @author leili
 * @date Apr 22, 2012
 */
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

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.sayln("OriginFieldList(");
		pr.indent(d + 1);
		pr.say(func.toString());
		pr.sayln(",");
		pr.indent(d + 1);
		pr.say(var.toString());
		if (next != null) {
			pr.sayln(",");
			next.printTree(pr, d + 1);
		}
		pr.say(")");
	}
}
