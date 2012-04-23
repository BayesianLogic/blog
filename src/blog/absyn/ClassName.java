package blog.absyn;

import blog.symbol.Symbol;

/**
 * @author leili
 * @date Apr 22, 2012
 * 
 */
public class ClassName extends Absyn {
	public Symbol name;

	public ClassName(int p, Symbol name) {
		this.name = name;
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.say("ClassName(");
		pr.say(name.toString());
		pr.say(")");
	}
}
