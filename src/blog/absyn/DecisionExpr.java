package blog.absyn;

/*added by cheng*/
public class DecisionExpr extends Expr {

	public DecisionExpr(int line, int col) {
		super(line, col);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void printTree(Printer pr, int d) {
		pr.sayln("ChoiceExpr");
		
	}
	
}
