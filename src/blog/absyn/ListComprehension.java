/**
 * 
 */
package blog.absyn;

/**
 * @author leili
 * @since Jun 24, 2014
 * 
 */
public class ListComprehension extends Expr {

  public ExprList setTuple;
  public FieldList enumVars;
  public Expr cond; // expression in condition

  public ListComprehension(int p, ExprList st, FieldList ev, Expr c) {
    this(0, p, st, ev, c);
  }

  public ListComprehension(int line, int col, ExprList st, FieldList ev, Expr c) {
    super(line, col);
    setTuple = st;
    enumVars = ev;

    cond = c;
  }

}
