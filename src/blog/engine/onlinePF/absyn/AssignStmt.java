package blog.engine.onlinePF.absyn;



public class AssignStmt extends Stmt {
	public String variable;
	public String assignment; 

	public AssignStmt(int line, int col, String x, String y) {
		super(line, col);
		variable = x;
		assignment = y;
	}


}
