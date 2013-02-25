package blog.absyn;

/*added by cheng*/

public class DecisionEvidence extends EvidenceStmt {
	public FuncCallExpr left;
	public Expr right;

	public DecisionEvidence(int line, int p, Expr s, Expr t) {
		super(line, p);
		left = (FuncCallExpr) s;
		right = t;
	}

	public DecisionEvidence(int p, FuncCallExpr s, Expr t) {
		this(0, p, s, t);
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.sayln("ChoiceEvidence(");
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
