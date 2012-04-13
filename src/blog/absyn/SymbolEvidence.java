package blog.absyn;

public class SymbolEvidence extends Evidence {
	public ImplicitSetExpr left;
	public ExplicitSetExpr right;
	
	public SymbolEvidence(int p, ImplicitSetExpr s, ExplicitSetExpr t) {
		pos = p;
		left = s;
		right = t;
	}
	
}
