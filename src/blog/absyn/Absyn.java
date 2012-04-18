package blog.absyn;

abstract public class Absyn {
	protected int pos;
	
	/**
	 * @return position of this abstract syntax tree in the input file
	 */
	public int getPos() {
		return pos;
	}

	abstract void printTree(Printer pr, int d);
}
