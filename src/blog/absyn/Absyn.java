package blog.absyn;

abstract public class Absyn {
	public int pos;
	public int line;
	public int col;

	/**
	 * @return position of this abstract syntax tree in the input file
	 */
	public int getPos() {
		return pos;
	}

	public int getLine() {
		return line;
	}

	public int getCol() {
		return col;
	}

	public abstract void printTree(Printer pr, int d);
}
