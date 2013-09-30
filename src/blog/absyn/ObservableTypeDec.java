package blog.absyn;

import blog.symbol.Symbol;

/**
 * @author Cheng
 * @date Apr 22, 2012
 */
public class ObservableTypeDec extends Dec {
	public Symbol name;

	/**
	 * @param p
	 * @param n
	 */
	public ObservableTypeDec(int p, Symbol n) {
		this(0, p, n);
	}

	public ObservableTypeDec(int line, int pos, Symbol n) {
		super(line, pos);
		name = n;
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.say("ObservableTypeDec(");
		pr.say(name.toString());
		pr.say(")");
	}
}
