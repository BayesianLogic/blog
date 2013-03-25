package blog.engine.onlinePF.absyn;


/**
 * Root class for abstract syntax tree, all nodes in the syntax tree should be
 * sub-type of this one.
 * 
 * The representation is independent of underlying engine and sampling method.
 * And is also independent of the intermediate representation of the model,
 * which including data structures for computing/sampling.
 * 
 * @author leili
 * @date Apr 22, 2012
 * 
 */

abstract public class Absyn {
	public int col;
	public int line;

	/**
	 * @param line
	 * @param col
	 */
	public Absyn(int line, int col) {
		this.line = line;
		this.col = col;
	}
	/**
	 * @return position of this abstract syntax tree in the input file
	 */
	public int getCol() {
		return col;
	}

	/**
	 * should directly use .line
	 * 
	 * @return
	 */
	public int getLine() {
		return line;
	}


}
