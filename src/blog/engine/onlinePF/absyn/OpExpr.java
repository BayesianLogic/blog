package blog.engine.onlinePF.absyn;

public class OpExpr extends Expr {
	public String query;
	public Number threshold;
	public Op op;


	public OpExpr(int currLineno, int currColno, String e1, Op op, Number e2) {
		super(currLineno,currColno);
		query = e1;
		threshold = e2;
		this.op = op;
	}

	public static enum Op {
		GEQ, LEQ, GT, LT, EQ, NEQ
	}

}
