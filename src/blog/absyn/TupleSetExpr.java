package blog.absyn;

/**
 * @author awong
 * @date Feb 11, 2013
 */
public class TupleSetExpr extends SetExpr {
  
  public ExprList setTuple;
  public FieldList enumVars;
  public Expr cond;
  
  public TupleSetExpr(int p, ExprList st, FieldList ev, Expr c) {
    this(0, p, st, ev, c);
  }
  
  public TupleSetExpr(int line, int col, ExprList st, FieldList ev, Expr c) {
    super(line, col);
    setTuple = st;
    enumVars = ev;
    cond = c;
  }
  
  @Override
  public void printTree(Printer pr, int d) {
    pr.indent(d);
    pr.sayln("TupleSetExpr(");
    setTuple.printTree(pr, d + 1);
    pr.sayln(",");
    enumVars.printTree(pr, d + 1);
    if (cond != null) {
      pr.sayln(",");
      cond.printTree(pr, d + 1);
    }
    pr.say(")");
  }

}
