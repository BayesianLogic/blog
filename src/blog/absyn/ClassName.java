package blog.absyn;

import blog.symbol.Symbol;

public class ClassName extends Absyn {
	public Symbol name;
	
	public ClassName(int p, String name) {
		this.name = Symbol.symbol(name);
	}
}
