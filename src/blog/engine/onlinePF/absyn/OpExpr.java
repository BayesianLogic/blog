package blog.engine.onlinePF.absyn;

public class OpExpr extends Expr {
	public String query;
	public Number threshold;
	public int oper;


	public OpExpr(int currLineno, int currColno, String e1, int op, Number e2) {
		super(currLineno,currColno);
		query = e1;
		threshold = e2;
		oper = op;
	}

	public final static int PLUS = 0, MINUS = 1, MULT = 2, DIV = 3, MOD = 4,
			POWER = 5, EQ = 11, NEQ = 12, LT = 13, LEQ = 14, GT = 15, GEQ = 16,
			AND = 21, OR = 22, NOT = 23, IMPLY = 24, SUB = 31, AT = 99;


}
