package blog.absyn;

public class StringExpr extends Expr {
	public String value;

	public StringExpr(int p, String v) {
		pos = p;
		value = v;
	}
}
