package blog.absyn;

public class OpExpr extends Expr {
	public Expr left, right;
	public int oper;

	public OpExpr(int p, Expr l, int o, Expr r) {
		pos = p;
		left = l;
		oper = o;
		right = r;
	}

	public final static int PLUS = 0, MINUS = 1, MULT = 2, DIV = 3, MOD = 4,
			EQ = 11, NEQ = 12, LT = 13, LEQ = 14, GT = 15, GEQ = 16, AND = 21,
			OR = 22, NOT = 23;
}
