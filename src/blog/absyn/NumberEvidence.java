package blog.absyn;

public class NumberEvidence extends EvidenceStmt {
	public NumberExpr num;
	public int size;

	public NumberEvidence(int p, NumberExpr s, int sz) {
		pos = p;
		num = s;
		size = sz;
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.sayln("NumberEvidence(");
		num.printTree(pr, d + 1);
		pr.sayln(",");
		pr.indent(d + 1);
		pr.say(size);
		pr.say(")");
	}
}
