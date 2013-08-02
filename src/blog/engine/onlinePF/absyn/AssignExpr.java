package blog.engine.onlinePF.absyn;

import blog.engine.onlinePF.absyn.OpExpr.Op;

public class AssignExpr extends Expr {
	public String variable;
	public String assignments;


	public AssignExpr(int currLineno, int currColno, String e1, String e2) {
		super(currLineno,currColno);
		variable = e1;
		assignments = e2;
	}

}
