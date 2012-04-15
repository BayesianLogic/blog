package blog.absyn;

public class MapTy extends Ty {
	public Ty from, to;

	public MapTy(int p, Ty f, Ty t) {
		pos = p;
		from = f;
		to = t;
	}

	@Override
	void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.sayln("MapTy(");
		from.printTree(pr, d + 1);
		pr.sayln(",");
		to.printTree(pr, d + 1);
		pr.say(")");
	}
}
