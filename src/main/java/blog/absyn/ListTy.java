package blog.absyn;

/**
 * abstract syntax node for List type
 * 
 * @author awong
 * @date August 9, 2012
 */
public class ListTy extends Ty {

	public Ty typ;

	public ListTy(int p, Ty t) {
		this(0, p, t);
	}

	public ListTy(int line, int col, Ty t) {
		super(line, col);
		typ = t;
	}
	
	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.say("ListTy<" + typ + ">");
	}
}
