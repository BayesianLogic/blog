package blog.absyn;

/**
 * Expression node in the abstract syntax tree
 * 
 * @author leili
 * @date Apr 22, 2012
 */
public abstract class Expr extends Absyn {

	/**
	 * @param line
	 * @param col
	 */
	public Expr(int line, int col) {
		super(line, col);
	}

	/**
	 * @param p
	 */
	public Expr(int p) {
		this(0, p);
	}

}
