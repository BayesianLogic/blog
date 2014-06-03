package blog.absyn;

/**
 * Node for declarations in blog, all declaration class should be sub-type of
 * this one.
 * 
 * @author leili
 * @date Apr 22, 2012
 * 
 */
abstract public class Dec extends Stmt {

	/**
	 * @param line
	 * @param pos
	 */
	public Dec(int line, int pos) {
		super(line, pos);
	}

	/**
	 * @deprecated
	 * @param p
	 */
	public Dec(int p) {
		super(p);
	}

}
