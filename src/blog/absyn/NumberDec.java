package blog.absyn;


/**
 * @author leili
 * @date Apr 22, 2012
 */
public class NumberDec extends Dec {
	public Ty typ;
	public OriginFieldList params;
	public Expr body;

	public NumberDec(int p, Ty n, OriginFieldList a, Expr b) {
		pos = p;
		typ = n;
		params = a;
		body = b;
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
