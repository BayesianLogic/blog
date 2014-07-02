/**
 * 
 */
package blog.absyn;

/**
 * @author leili
 * @since Jun 22, 2014
 * 
 */
public class CaseExpr extends Expr {

  public Expr test;
  public ExprTupleList clauses;

  /**
   * @param line
   * @param col
   */
  public CaseExpr(int line, int col, Expr test, ExprTupleList clauses) {
    super(line, col);
    this.test = test;
    this.clauses = clauses;
  }

  /**
   * @param p
   */
  public CaseExpr(int p, Expr test, ExprTupleList clauses) {
    super(p);
    this.test = test;
    this.clauses = clauses;
  }

}
