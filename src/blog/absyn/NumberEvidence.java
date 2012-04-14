package blog.absyn;

public class NumberEvidence extends Evidence {
	public NumberExpr num;
	public int size;
	
	public NumberEvidence(int p, NumberExpr s, int sz) {
		pos = p;
		num = s;
		size = sz;
	}
	
}
