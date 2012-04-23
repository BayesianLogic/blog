package blog.absyn;

/**
 * @author leili
 * @date Apr 22, 2012
 * 
 */
public class DistinctSymbolDec extends Dec {
	public SymbolArrayList symbols;
	public Ty type;

	public DistinctSymbolDec(int p, Ty t, SymbolArrayList ss) {
		pos = p;
		type = t;
		symbols = ss;
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.sayln("DistinctSymbolDec(");
		type.printTree(pr, d + 1);
		pr.sayln(",");
		symbols.printTree(pr, d + 1);
		pr.say(")");
	}
}
