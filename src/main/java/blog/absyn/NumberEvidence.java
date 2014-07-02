package blog.absyn;

/**
 * @author leili
 * @date Apr 22, 2012
 */
public class NumberEvidence extends EvidenceStmt {
	public NumberExpr num;
	public int size;

	public NumberEvidence(int line, int pos, NumberExpr s, int sz) {
		super(line, pos);
		num = s;
		size = sz;
	}

	public NumberEvidence(int pos, NumberExpr s, int sz) {
		this(0, pos, s, sz);
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
