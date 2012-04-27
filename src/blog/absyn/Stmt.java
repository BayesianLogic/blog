package blog.absyn;

/**
 * @author leili
 * @date Apr 22, 2012
 */
abstract public class Stmt extends Absyn {

	/**
	 * @param line
	 * @param pos
	 */
	public Stmt(int line, int pos) {
		super(line, pos);
	}

	/**
	 * @param p
	 */
	public Stmt(int p) {
		this(0, p);
	}
}
