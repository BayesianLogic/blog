package blog.absyn;

public class NumberEvidence extends Evidence {
	public ImplicitSetExpr set;
	public int size;
	
	public NumberEvidence(int p, ImplicitSetExpr s, int sz) {
		pos = p;
		set = s;
		size = sz;
	}
	
}
