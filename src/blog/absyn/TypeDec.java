package blog.absyn;

import blog.symbol.Symbol;

/**
 * @author leili
 * @date Apr 22, 2012
 */
public class TypeDec extends Dec {
	public Symbol name;

	public TypeDec(int p, Symbol n) {
		pos = p;
		name = n;
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.say("TypeDec(");
		pr.say(name.toString());
		pr.say(")");
	}
}
