package blog.absyn;

/**
 * @author leili
 * @date Apr 22, 2012
 */
abstract public class SetExpr extends Expr {

	/**
	 * @param line
	 * @param col
	 */
	public SetExpr(int line, int col) {
		super(line, col);
	}

	/**
	 * @param p
	 */
	public SetExpr(int p) {
		this(0, p);
	}
}
