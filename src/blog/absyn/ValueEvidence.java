package blog.absyn;

public class ValueEvidence extends Evidence {
	public Expr left;
	public Expr right;
	
	public ValueEvidence(int p, Expr s, Expr t) {
		pos = p;
		left = s;
		right = t;
	}
	
}
