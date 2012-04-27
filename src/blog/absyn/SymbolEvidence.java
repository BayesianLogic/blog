package blog.absyn;

/**
 * @author leili
 * @date Apr 22, 2012
 */
public class SymbolEvidence extends EvidenceStmt {
	public ImplicitSetExpr left;
	public ExplicitSetExpr right;

	public SymbolEvidence(int line, int p, ImplicitSetExpr s, ExplicitSetExpr t) {
		super(line, p);
		left = s;
		right = t;
	}

	public SymbolEvidence(int p, ImplicitSetExpr s, ExplicitSetExpr t) {
		this(0, p, s, t);
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.sayln("SymbolEvidence(");
		if (left != null) {
			left.printTree(pr, d + 1);
		}
		if (right != null) {
			pr.sayln(",");
			right.printTree(pr, d + 1);
		}
		pr.say(")");
	}
}
