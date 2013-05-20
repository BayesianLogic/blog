package blog.engine.onlinePF.absyn;

public class ActionStmt extends Stmt{
	public String action;
	
	public ActionStmt(int line, int pos, String decision) {
		super(line, pos);
		action = decision;
	}

}
