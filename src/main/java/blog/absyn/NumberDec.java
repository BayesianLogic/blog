package blog.absyn;

/**
 * @author leili
 * @date Apr 22, 2012
 */
public class NumberDec extends Dec {
	public Ty typ;
	public OriginFieldList params;
	public Expr body;

	public NumberDec(int line, int pos, Ty n, OriginFieldList a, Expr b) {
		super(line, pos);
		typ = n;
		params = a;
		body = b;
	}

	public NumberDec(int pos, Ty n, OriginFieldList a, Expr b) {
		this(0, pos, n, a, b);
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.sayln("NumberDec(");
		typ.printTree(pr, d + 1);
		if (params != null) {
			pr.sayln(",");
			params.printTree(pr, d + 1);
		}
		pr.sayln(",");
		body.printTree(pr, d + 1);
	}
}
