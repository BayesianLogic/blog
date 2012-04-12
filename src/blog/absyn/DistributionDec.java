package blog.absyn;

import blog.symbol.Symbol;

public class DistributionDec extends Dec {
	public Symbol name;
	public ClassName classname;
	public ExprList params;

	public DistributionDec(int p, Symbol n, ClassName cn, ExprList a) {
		pos = p;
		name = n;
		classname = cn;
		params = a;
	}
}
