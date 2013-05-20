package blog.engine.onlinePF.absyn;


public class IfStmt extends Stmt {
	public Expr test;
	public Stmt thenclause;
	public Stmt elseclause; /* optional */


	public IfStmt(int p, Expr x, Stmt y, Stmt z) {
		this(0, p, x, y, z);
	}

	public IfStmt(int line, int col, Expr x, Stmt y, Stmt z) {
		super(line, col);
		test = x;
		thenclause = y;
		elseclause = z;
	}


}
