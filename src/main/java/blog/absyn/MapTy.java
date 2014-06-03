package blog.absyn;

/**
 * @author leili
 * @date Apr 22, 2012
 */
public class MapTy extends Ty {
	public Ty from, to;

	public MapTy(int p, Ty f, Ty t) {
		this(0, p, f, t);
	}

	public MapTy(int line, int col, Ty f, Ty t) {
		super(line, col);
		from = f;
		to = t;
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.sayln("MapTy(");
		from.printTree(pr, d + 1);
		pr.sayln(",");
		to.printTree(pr, d + 1);
		pr.say(")");
	}
}
