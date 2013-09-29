package blog.engine.onlinePF.absyn;


public class ForStmt extends Stmt {
	public String type;
	public String var;
	public Stmt body;


	public ForStmt(int p, String x, String y, Stmt z) {
		this(0, p, x, y, z);
	}

	public ForStmt(int line, int col, String x, String y, Stmt z) {
		super(line, col);
		type = x;
		var = y;
		body = z;
	}


}
