package blog.absyn;

import blog.symbol.Symbol;

/**
 * @author leili
 * @date Apr 22, 2012
 * 
 */
public class ClassName extends Absyn {
	public Symbol name;

	public ClassName(int line, int p, Symbol name) {
		super(line, p);
		this.name = name;
	}

	public ClassName(int p, Symbol name) {
		this(0, p, name);
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.say("ClassName(");
		pr.say(name.toString());
		pr.say(")");
	}
}
