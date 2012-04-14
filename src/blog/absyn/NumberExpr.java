package blog.absyn;

public class NumberExpr extends Evidence {
	public SetExpr set;
	
	public NumberExpr(int p, SetExpr s) {
		pos = p;
		set = s;
	}
	
}
