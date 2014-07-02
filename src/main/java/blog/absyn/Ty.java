package blog.absyn;

/**
 * @author leili
 * @date Apr 22, 2012
 */
abstract public class Ty extends Absyn {

	/**
	 * @param line
	 * @param col
	 */
	public Ty(int line, int col) {
		super(line, col);
	}

	/**
	 * @param p
	 */
	public Ty(int p) {
		this(0, p);
	}
}
