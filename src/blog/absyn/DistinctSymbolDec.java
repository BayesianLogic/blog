package blog.absyn;

import blog.symbol.Symbol;

public class DistinctSymbolDec extends Dec {
	public SymbolArrayList symbols;
	public Ty type;

	public DistinctSymbolDec(int p, Ty t, SymbolArrayList ss) {
		pos = p;
		type = t;
		symbols = ss;
	}
}
