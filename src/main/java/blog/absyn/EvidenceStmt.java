package blog.absyn;

/**
 * @author leili
 * @date Apr 22, 2012
 * 
 */
abstract public class EvidenceStmt extends Stmt {

	/**
	 * @param p
	 */
	public EvidenceStmt(int p) {
		this(0, p);
	}

	/**
	 * @param line
	 * @param pos
	 */
	public EvidenceStmt(int line, int pos) {
		super(line, pos);
	}
}
