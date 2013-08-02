package blog.engine.onlinePF.absyn;


public class ForStmt extends Stmt {
	public Expr assignment;
	public Stmt body; 

	public ForStmt(int line, int col, Expr x, Stmt y) {
		super(line, col);
		assignment = x;
		body = y;
	}


}
