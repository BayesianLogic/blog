package blog.absyn;

public class OpExpr extends Expr {
	public Expr left, right;
	public int oper;

	public OpExpr(int p, Expr l, int o, Expr r) {
		pos = p;
		left = l;
		oper = o;
		right = r;
	}

	public final static int PLUS = 0, MINUS = 1, MULT = 2, DIV = 3, MOD = 4,
			EQ = 11, NEQ = 12, LT = 13, LEQ = 14, GT = 15, GEQ = 16, AND = 21,
			OR = 22, NOT = 23, SUB = 31;

	@Override
	public void printTree(Printer pr, int d) {
		pr.indent(d);
		pr.sayln("OpExpr(");
		pr.indent(d + 1);
		switch (oper) {
		case OpExpr.PLUS:
			pr.say("PLUS");
			break;
		case OpExpr.MINUS:
			pr.say("MINUS");
			break;
		case OpExpr.MULT:
			pr.say("MULT");
			break;
		case OpExpr.DIV:
			pr.say("DIV");
			break;
		case OpExpr.MOD:
			pr.say("MOD");
			break;
		case OpExpr.EQ:
			pr.say("EQ");
			break;
		case OpExpr.NEQ:
			pr.say("NEQ");
			break;
		case OpExpr.LT:
			pr.say("LT");
			break;
		case OpExpr.LEQ:
			pr.say("LEQ");
			break;
		case OpExpr.GT:
			pr.say("GT");
			break;
		case OpExpr.GEQ:
			pr.say("GEQ");
			break;
		case OpExpr.AND:
			pr.say("AND");
			break;
		case OpExpr.OR:
			pr.say("OR");
			break;
		case OpExpr.NOT:
			pr.say("NOT");
			break;
		case OpExpr.SUB:
			pr.say("SUB");
			break;
		default:
			throw new Error("Print.prExp.OpExpr");
		}
		if (left != null) {
			pr.sayln(",");
			left.printTree(pr, d + 1);
		}
		if (right != null) {
			pr.sayln(",");
			right.printTree(pr, d + 1);
		}
		pr.say(")");
	}
}
